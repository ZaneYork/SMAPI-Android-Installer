package com.zane.smapiinstaller.logic

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.documentfile.provider.DocumentUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.google.common.io.ByteStreams
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.MainApplication
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.constant.ManifestPatchConstants
import com.zane.smapiinstaller.entity.ApkFilesManifest
import com.zane.smapiinstaller.entity.ManifestEntry
import com.zane.smapiinstaller.logic.ActivityResultHandler.registerListener
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.StringUtils
import com.zane.smapiinstaller.utils.ZipUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.zeroturnaround.zip.ZipUtil
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.Channels
import java.util.Collections

/**
 * 通用逻辑
 *
 * @author Zane
 */
object CommonLogic {
    /**
     * 从View获取所属Activity
     *
     * @param view context容器
     * @return Activity
     */
    @JvmStatic
    fun getActivityFromView(view: View?): Activity? {
        if (null != view) {
            var context = view.context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
        }
        return null
    }

    /**
     * 从一个View获取Application
     *
     * @param view 控件
     * @return Application
     */
    @JvmStatic
    fun getApplicationFromView(view: View?): MainApplication? {
        val activity = getActivityFromView(view)
        return if (null != activity) {
            activity.application as MainApplication
        } else null
    }

    /**
     * 在UI线程执行操作
     *
     * @param activity activity
     * @param action   操作
     */
    @JvmStatic
    fun runOnUiThread(activity: Activity?, action: (Activity) -> Unit) {
        activity?.let {
            if (!it.isFinishing) {
                it.runOnUiThread { action.invoke(it) }
            }
        }
    }

    /**
     * 打开指定URL
     *
     * @param context context
     * @param url     目标URL
     */
    @JvmStatic
    fun openUrl(context: Context, url: String?) {
        try {
            val intent = Intent()
            intent.data = Uri.parse(url)
            intent.action = Intent.ACTION_VIEW
            context.startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    /**
     * 复制文本到剪贴板
     *
     * @param context 上下文
     * @param copyStr 文本
     * @return 是否复制成功
     */
    @JvmStatic
    fun copyToClipboard(context: Context, copyStr: String?): Boolean {
        return try {
            context.getSystemService(Context.CLIPBOARD_SERVICE)?.let { cm ->
                val mClipData = ClipData.newPlainText("Label", copyStr)
                (cm as ClipboardManager).setPrimaryClip(mClipData)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 扫描全部兼容包
     *
     * @param context context
     * @return 兼容包列表
     */
    fun findAllApkFileManifest(context: Context): MutableList<ApkFilesManifest> {
        val apkFilesManifest =
            FileUtils.getAssetJson(context, "apk_files_manifest.json", ApkFilesManifest::class.java)
        val apkFilesManifests =
            apkFilesManifest?.let { Lists.newArrayList(apkFilesManifest) } ?: Lists.newArrayList()
        val compatFolder = File(context.filesDir, "compat")
        if (compatFolder.exists()) {
            compatFolder.listFiles { obj -> obj.isDirectory }?.let {
                for (directory in it) {
                    val manifestFile = File(directory, "apk_files_manifest.json")
                    if (manifestFile.exists()) {
                        val manifest =
                            FileUtils.getFileJson(manifestFile, ApkFilesManifest::class.java)
                        if (manifest != null) {
                            apkFilesManifests.add(manifest)
                        }
                    }
                }
            }
        }
        apkFilesManifests.sortWith(Comparator sort@{ a, b ->
            if (a.targetPackageName != null && b.targetPackageName == null) {
                return@sort -1
            } else if (b.targetPackageName != null) {
                return@sort b.minBuildCode.compareTo(a.minBuildCode)
            }
            1
        })
        return apkFilesManifests
    }

    @JvmStatic
    fun computePackageName(packageInfo: PackageInfo): String {
        var packageName = packageInfo.packageName
        if (StringUtils.endsWith(
                packageInfo.versionName, ManifestPatchConstants.PATTERN_VERSION_AMAZON
            )
        ) {
            packageName =
                ManifestPatchConstants.APP_PACKAGE_NAME + ManifestPatchConstants.PATTERN_VERSION_AMAZON
        }
        return packageName
    }

    /**
     * 提取SMAPI环境文件到内部存储对应位置
     *
     * @param context     context
     * @param apkPath     安装包路径
     * @param checkMode   是否为校验模式
     * @param packageName 包名
     * @param versionCode 版本号
     * @return 操作是否成功
     */
    @JvmStatic
    fun unpackSmapiFiles(
        context: Activity,
        apkPath: String,
        checkMode: Boolean,
        packageName: String?,
        versionCode: Long
    ): Boolean {
        val apkFilesManifests = findAllApkFileManifest(context)
        filterManifest(apkFilesManifests, packageName, versionCode)
        var manifestEntries: List<ManifestEntry>? = null
        var apkFilesManifest: ApkFilesManifest? = null
        if (apkFilesManifests.size > 0) {
            apkFilesManifest = apkFilesManifests[0]
            val basePath = apkFilesManifests[0].basePath
            if (StringUtils.isNoneBlank(basePath)) {
                manifestEntries = FileUtils.getAssetJson(context,
                    basePath + "smapi_files_manifest.json",
                    object : TypeReference<List<ManifestEntry>>() {})
            }
        }
        if (manifestEntries == null) {
            manifestEntries = FileUtils.getAssetJson(context,
                "smapi_files_manifest.json",
                object : TypeReference<List<ManifestEntry>>() {})
        }
        if (manifestEntries == null) {
            return false
        }
        val basePath = File(FileUtils.stadewValleyBasePath + "/StardewValley/")
        if (!basePath.exists()) {
            if (!basePath.mkdir()) {
                return false
            }
        } else {
            if (!checkMode) {
                val oldAssemblies = File(
                    basePath, "smapi-internal"
                ).listFiles(WildcardFileFilter("*.dll") as FileFilter)
                if (oldAssemblies != null) {
                    for (file in oldAssemblies) {
                        org.zeroturnaround.zip.commons.FileUtils.deleteQuietly(file)
                    }
                }
            }
        }
        val noMedia = File(basePath, ".nomedia")
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile()
            } catch (ignored: IOException) {
            }
        }
        for (entry in manifestEntries) {
            val targetFile = File(basePath, entry.targetPath)
            when (entry.origin) {
                0 -> unpackFromInstaller(
                    context, checkMode, apkFilesManifest, basePath, entry, targetFile
                )

                1 -> unpackFromApk(apkPath, checkMode, entry, targetFile)
                else -> {}
            }
        }
        if (checkDataRootPermission(context)) {
            val targetDirUri = pathToTreeUri(Constants.TARGET_DATA_FILE_URI)
            val documentFile = DocumentFile.fromTreeUri(context, targetDirUri)
            val filesDoc = DocumentUtils.findFile(context, documentFile, "files")
            if (filesDoc != null) {
                copyDocument(context, File(basePath, "smapi-internal"), filesDoc)
            }
        }
        return true
    }

    @JvmStatic
    fun copyDocument(context: Activity, src: File, dest: DocumentFile) {
        if (src.isDirectory) {
            val documentFile =
                DocumentUtils.findFile(context, dest, src.name) ?: dest.createDirectory(src.name)
            documentFile?.let {
                src.listFiles()?.let {
                    for (file in it) {
                        copyDocument(context, file, documentFile)
                    }
                }
            }
        } else {
            val documentFile = DocumentUtils.findFile(context, dest, src.name) ?: dest.createFile(
                "application/x-binary", src.name
            )
            documentFile?.let {
                if (documentFile.length() != src.length()) {
                    try {
                        context.contentResolver.openOutputStream(documentFile.uri)
                            .use { outputStream ->
                                org.zeroturnaround.zip.commons.FileUtils.copy(
                                    src, outputStream
                                )
                            }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    private fun checkMusic(context: Context): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val pathFrom = File(
                FileUtils.stadewValleyBasePath,
                "Android/obb/" + Constants.ORIGIN_PACKAGE_NAME_GOOGLE
            )
            val pathTo = File(FileUtils.stadewValleyBasePath, "StardewValley")
            if (pathFrom.exists() && pathFrom.isDirectory) {
                if (!checkObbRootPermission(
                        context as Activity,
                        ActivityResultHandler.REQUEST_CODE_OBB_FILES_ACCESS_PERMISSION
                    ) { checkMusic(context) }
                ) {
                    return false
                }
                if (!pathTo.exists()) {
                    pathTo.mkdirs()
                }
                val files =
                    pathFrom.listFiles { _, name -> name.contains("com.chucklefish.stardewvalley.obb") }
                if (files != null) {
                    for (file in files) {
                        try {
                            val targetFile = File(pathTo, file.name)
                            if (!targetFile.exists() || org.zeroturnaround.zip.commons.FileUtils.sizeOf(
                                    targetFile
                                ) != org.zeroturnaround.zip.commons.FileUtils.sizeOf(
                                    file
                                )
                            ) {
                                org.zeroturnaround.zip.commons.FileUtils.copyFile(file, targetFile)
                            }
                        } catch (e: IOException) {
                            Crashes.trackError(e)
                        }
                    }
                }
            }
        }
        return true
    }

    private fun unpackFromApk(
        apkPath: String, checkMode: Boolean, entry: ManifestEntry, targetFile: File
    ) {
        if (!checkMode || !targetFile.exists()) {
            if (entry.isXALZ) {
                var bytes = ZipUtil.unpackEntry(File(apkPath), entry.assetPath)
                if (entry.isXALZ) {
                    bytes = ZipUtils.decompressXALZ(bytes)
                }
                try {
                    org.zeroturnaround.zip.commons.FileUtils.openOutputStream(targetFile)
                        .use { outputStream ->
                            ByteStreams.copy(
                                Channels.newChannel(ByteArrayInputStream(bytes)),
                                outputStream.channel
                            )
                        }
                } catch (ignore: IOException) {
                }
            } else {
                ZipUtil.unpack(File(apkPath), targetFile) { name ->
                    if (name.startsWith(
                            entry.assetPath
                        )
                    ) FilenameUtils.getName(name) else null
                }
            }
        }
    }

    private fun unpackFromInstaller(
        context: Context,
        checkMode: Boolean,
        apkFilesManifest: ApkFilesManifest?,
        basePath: File,
        entry: ManifestEntry,
        targetFile: File
    ) {
        if (entry.isExternal && apkFilesManifest != null) {
            val bytes =
                FileUtils.getAssetBytes(context, apkFilesManifest.basePath + entry.assetPath)
            try {
                FileOutputStream(targetFile).use { outputStream ->
                    ByteStreams.copy(
                        Channels.newChannel(
                            ByteArrayInputStream(bytes)
                        ), outputStream.channel
                    )
                }
            } catch (ignored: IOException) {
            }
        } else {
            if (entry.targetPath.endsWith("/") && entry.assetPath.contains("*")) {
                val path = StringUtils.substring(
                    entry.assetPath, 0, StringUtils.lastIndexOf(entry.assetPath, "/")
                )
                val pattern = StringUtils.substringAfterLast(entry.assetPath, "/")
                try {
                    context.assets.list(path)?.filter { filename ->
                        StringUtils.wildCardMatch(
                            filename, pattern
                        )
                    }?.forEach { filename ->
                        unpackFile(
                            context,
                            checkMode,
                            "$path/$filename",
                            File(basePath, entry.targetPath + filename)
                        )
                    }
                } catch (ignored: IOException) {
                }
            } else {
                unpackFile(context, checkMode, entry.assetPath, targetFile)
            }
        }
    }

    fun filterManifest(
        manifests: MutableList<ApkFilesManifest>, packageName: String?, versionCode: Long
    ) {
        Iterables.removeIf(manifests) { manifest ->
            if (manifest == null) {
                return@removeIf true
            }
            if (versionCode < manifest.minBuildCode) {
                return@removeIf true
            }
            if (manifest.maxBuildCode != null) {
                if (versionCode > manifest.maxBuildCode) {
                    return@removeIf true
                }
            }
            manifest.targetPackageName != null && packageName != null && !manifest.targetPackageName.contains(
                packageName
            )
        }
    }

    private fun unpackFile(
        context: Context, checkMode: Boolean, assertPath: String, targetFile: File
    ) {
        if (!checkMode || !targetFile.exists()) {
            try {
                context.assets.open(assertPath).use { inputStream ->
                    targetFile.parentFile?.let {
                        if (!it.exists()) {
                            if (!it.mkdirs()) {
                                Log.e("COMMON", "Make dirs error")
                                return
                            }
                        }
                    }
                    FileOutputStream(targetFile).use { outputStream ->
                        ByteStreams.copy(
                            Channels.newChannel(
                                inputStream
                            ), outputStream.channel
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e("COMMON", "Copy Error", e)
            }
        }
    }

    @JvmStatic
    fun showAnimation(view: ImageView, anim: Int, action: (Animation?) -> Unit) {
        val animation = AnimationUtils.loadAnimation(getActivityFromView(view), anim)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                action.invoke(animation)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(animation)
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    fun getVersionCode(activity: Activity): Long {
        try {
            val manager = activity.packageManager
            val info = manager.getPackageInfo(activity.packageName, 0)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else info.versionCode.toLong()
        } catch (ignored: Exception) {
        }
        return Int.MAX_VALUE.toLong()
    }

    /**
     * 在谷歌商店打开
     *
     * @param activity activity
     */
    fun openInPlayStore(activity: Activity) {
        activity.let { context ->
            try {
                val intent = Intent("android.intent.action.VIEW")
                intent.data = Uri.parse("market://details?id=com.zane.smapiinstaller")
                intent.setPackage("com.android.vending")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (ex: Exception) {
                openUrl(
                    activity,
                    "https://play.google.com/store/apps/details?id=com.zane.smapiinstaller"
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    fun openPermissionSetting(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse("package:" + activity.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivityForResult(
                intent, ActivityResultHandler.REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION
            )
        } catch (ignored: ActivityNotFoundException) {
            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                activity.startActivityForResult(
                    intent, ActivityResultHandler.REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION
                )
            } catch (ignored2: ActivityNotFoundException) {
                intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivityForResult(
                    intent, ActivityResultHandler.REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION
                )
            }
        }
    }

    @JvmStatic
    fun checkDataRootPermission(context: Context): Boolean {
        val pathFrom = File(
            FileUtils.stadewValleyBasePath,
            "Android/data/" + Constants.TARGET_PACKAGE_NAME + "/files/"
        )
        if (!pathFrom.exists()) {
            return false
        }
        val targetDirUri = pathToTreeUri(Constants.TARGET_DATA_FILE_URI)
        return checkPathPermission(context, targetDirUri)
    }

    @JvmStatic
    fun requestDataRootPermission(
        context: Activity, REQUEST_CODE_FOR_DIR: Int, callback: (Boolean) -> Unit
    ): Boolean {
        val pathFrom = File(
            FileUtils.stadewValleyBasePath,
            "Android/data/" + Constants.TARGET_PACKAGE_NAME + "/files"
        )
        if (!pathFrom.exists()) {
            return true
        }
        val targetDirUri = pathToTreeUri(Constants.TARGET_DATA_FILE_URI)
        if (checkPathPermission(context, targetDirUri)) {
            return true
        }
        registerListener(ActivityResultHandler.REQUEST_CODE_DATA_FILES_ACCESS_PERMISSION) { resultCode, data ->
            takePermission(
                resultCode, data, context.contentResolver, callback
            )
        }
        val documentFile = DocumentFile.fromTreeUri(context, targetDirUri)
        val intent1 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent1.flags =
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile?.uri)
        }
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR)
        return false
    }

    private fun takePermission(
        resultCode: Int, data: Intent?, context: ContentResolver, callback: (Boolean) -> Unit
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return
            }
            val uri = data.data ?: return
            context.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            callback.invoke(true)
        } else {
            callback.invoke(false)
        }
    }

    @JvmStatic
    fun pathToTreeUri(path: String): Uri {
        return Uri.parse(
            "content://com.android.externalstorage.documents/tree/primary%3A" + path.replace(
                "/", "%2F"
            )
        )
    }

    fun pathToSingleUri(path: String): Uri {
        return Uri.parse(
            "content://com.android.externalstorage.documents/document/primary%3A" + path.replace(
                "/", "%2F"
            )
        )
    }

    fun checkPathPermission(context: Context, targetDirUri: Uri): Boolean {
        return DocumentFile.fromTreeUri(context, targetDirUri)?.canWrite() ?: false
    }

    fun checkObbRootPermission(
        context: Activity, REQUEST_CODE_FOR_DIR: Int, callback: (Boolean) -> Unit
    ): Boolean {
        val targetDirUri = pathToTreeUri("Android/obb")
        if (checkPathPermission(context, targetDirUri)) {
            return true
        }
        registerListener(ActivityResultHandler.REQUEST_CODE_OBB_FILES_ACCESS_PERMISSION) { resultCode, data ->
            takePermission(
                resultCode, data, context.contentResolver, callback
            )
        }
        val documentFile = DocumentFile.fromTreeUri(context, targetDirUri)
        val intent1 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent1.flags =
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile?.uri)
        }
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR)
        return false
    }

    @JvmStatic
    fun showPrivacyPolicy(
        view: View, callback: (MaterialDialog?, DialogAction) -> Unit = { _, _ -> }
    ) {
        val context = view.context
        val policy = FileUtils.getLocaledAssetText(context, "privacy_policy.txt")
        DialogUtils.showConfirmDialog(
            view, R.string.privacy_policy, policy, R.string.confirm, R.string.cancel, true, callback
        )
    }

    @JvmStatic
    fun showProgressDialog(
        root: View?, context: Context, dialogConsumer: (ProgressDialog) -> Unit
    ) {
        val dialogHolder = DialogUtils.showProgressDialog(
            root, R.string.install_progress_title, context.getString(R.string.extracting_package)
        )
        var dialog: ProgressDialog? = null
        try {
            do {
                Thread.sleep(10)
                dialog = dialogHolder.get()
            } while (dialog == null)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val configManager = ConfigManager()
                if (configManager.config.isInitial) {
                    configManager.config.isInitial = false
                    configManager.config.isDisableMonoMod = true
                    configManager.flushConfig()
                }
            }
            dialogConsumer.invoke(dialog)
        } catch (ignored: InterruptedException) {
        } catch (e: Exception) {
            Crashes.trackError(e)
            DialogUtils.showAlertDialog(root, R.string.error, e.localizedMessage)
        } finally {
            DialogUtils.dismissDialog(root, dialog)
        }
    }
}