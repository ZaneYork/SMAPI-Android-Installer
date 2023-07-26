package com.zane.smapiinstaller.logic

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.apksig.ApkSigner
import com.android.apksig.ApkVerifier
import com.android.apksig.DefaultApkSignerEngine
import com.android.apksig.util.DataSources
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.base.Stopwatch
import com.google.common.base.Ticker
import com.google.common.io.ByteStreams
import com.zane.smapiinstaller.BuildConfig
import com.zane.smapiinstaller.MainActivity
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.constant.ManifestPatchConstants
import com.zane.smapiinstaller.dto.Tuple2
import com.zane.smapiinstaller.entity.ApkFilesManifest
import com.zane.smapiinstaller.entity.ManifestEntry
import com.zane.smapiinstaller.logic.ActivityResultHandler.registerListener
import com.zane.smapiinstaller.logic.ManifestTagVisitor.AttrArgs
import com.zane.smapiinstaller.logic.ManifestTagVisitor.ChildArgs
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.ManifestUtil
import com.zane.smapiinstaller.utils.StringUtils
import com.zane.smapiinstaller.utils.ZipUtils
import net.fornwall.apksigner.KeyStoreFileManager.JksKeyStore
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.NotImplementedException
import org.zeroturnaround.zip.ZipUtil
import pxb.android.axml.NodeVisitor
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.Deflater

/**
 * @author Zane
 */
class ApkPatcher(context: Context) {
    private val context: Context

    /**
     * 获取报错内容
     *
     * @return 报错内容
     */
    val errorMessage = AtomicReference<String?>()
    private val gamePackageName = AtomicReference<String?>()
    private val gameVersionCode = AtomicLong()
    val switchAction = AtomicInteger()
    private var originSignInfo: Tuple2<ByteArray, Set<String>>? = null
    private val progressListener: MutableList<(Int) -> Unit> = ArrayList()
    private var lastProgress = -1
    private val stopwatch = Stopwatch.createUnstarted(object : Ticker() {
        override fun read(): Long {
            return SystemClock.elapsedRealtimeNanos()
        }
    })

    init {
        lastProgress = -1
        this.context = context
    }

    /**
     * 依次扫描package_names.json文件对应的包名，抽取找到的第一个游戏APK到SMAPI Installer路径
     *
     * @param advancedStage 0: 初始化，1: 高级安装，-1: 普通安装
     * @return 抽取后的APK文件路径，如果抽取失败返回null
     */
    fun extract(advancedStage: Int): Tuple2<String, Array<String?>>? {
        emitProgress(0)
        val packageManager = context.packageManager
        val packageNames = FileUtils.getAssetJson(
            context,
            "package_names.json",
            object : TypeReference<List<String>>() {})
        if (packageNames == null) {
            errorMessage.set(context.getString(R.string.error_game_not_found))
            return null
        }
        for (packageName in packageNames) {
            return try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val sourceDir = packageInfo.applicationInfo.publicSourceDir
                gamePackageName.set(CommonLogic.computePackageName(packageInfo))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    gameVersionCode.set(packageInfo.longVersionCode)
                } else {
                    gameVersionCode.set(packageInfo.versionCode.toLong())
                }
                val apkFile = File(sourceDir)
                val stadewValleyBasePath = FileUtils.stadewValleyBasePath
                if (advancedStage == 0) {
                    val count = AtomicInteger()
                    ZipUtil.unpack(
                        apkFile, File("$stadewValleyBasePath/StardewValley/")
                    ) { name ->
                        if (name.startsWith("assets/")) {
                            val progress = count.incrementAndGet()
                            if (progress % 30 == 0) {
                                emitProgress(progress / 30)
                            }
                            return@unpack name.replaceFirst("assets/".toRegex(), "")
                        }
                        null
                    }
                    return Tuple2(apkFile.absolutePath, arrayOfNulls(0))
                } else if (advancedStage == 1) {
                    val contentFolder = File("$stadewValleyBasePath/StardewValley/Content")
                    if (contentFolder.exists()) {
                        if (!contentFolder.isDirectory) {
                            errorMessage.set(
                                context.getString(
                                    R.string.error_directory_exists_with_same_filename,
                                    contentFolder.absolutePath
                                )
                            )
                            return null
                        }
                    } else {
                        extract(0)
                    }
                    return Tuple2(apkFile.absolutePath, arrayOfNulls(0))
                }
                emitProgress(5)
                Tuple2(apkFile.absolutePath, packageInfo.applicationInfo.splitSourceDirs)
            } catch (ignored: PackageManager.NameNotFoundException) {
                null
            }
        }
        errorMessage.set(context.getString(R.string.error_game_not_found))
        return null
    }

    /**
     * 将指定APK文件重新打包，添加SMAPI，修改AndroidManifest.xml，同时验证版本是否正确
     *
     * @param apkPath    APK文件路径
     * @param targetFile 目标文件
     * @param isAdvanced 是否高级模式
     * @return 是否成功打包
     */
    fun patch(
        apkPath: String?,
        resourcePacks: Array<String?>?,
        targetFile: File,
        isAdvanced: Boolean,
        isResourcePack: Boolean
    ): Boolean {
        if (apkPath == null) {
            return false
        }
        val file = File(apkPath)
        if (!file.exists()) {
            return false
        }
        try {
            val manifest = ZipUtil.unpackEntry(file, "AndroidManifest.xml")
            emitProgress(9)
            val apkFilesManifests = CommonLogic.findAllApkFileManifest(context)
            val modifiedManifest = modifyManifest(manifest, apkFilesManifests)
            if (apkFilesManifests.isEmpty()) {
                errorMessage.set(context.getString(R.string.error_no_supported_game_version))
                switchAction.set(R.string.menu_download)
                return false
            }
            if (modifiedManifest == null) {
                errorMessage.set(context.getString(R.string.failed_to_process_manifest))
                return false
            }
            val apkFilesManifest = apkFilesManifests[0]
            val manifestEntries = apkFilesManifest.manifestEntries
            errorMessage.set(null)
            val entries: MutableList<ZipUtils.ZipEntrySource> =
                if (isResourcePack) ArrayList() else manifestEntries.mapNotNull { entry ->
                    processFileEntry(
                        file, apkFilesManifest, entry, isAdvanced
                    )
                }.flatMap { list -> list.asList() }.filterNotNull().distinct().toMutableList()
            entries.add(
                ZipUtils.ZipEntrySource(
                    "AndroidManifest.xml", modifiedManifest, Deflater.DEFLATED
                )
            )
            emitProgress(10)
            val baseProgress = 10
            stopwatch.reset()
            stopwatch.start()
            originSignInfo = ZipUtils.addOrReplaceEntries(apkPath,
                resourcePacks,
                entries,
                targetFile.absolutePath,
                { entryName ->
                    entryName.startsWith("assemblies/assemblies.") || isAdvanced && entryName.startsWith(
                        "assets/Content"
                    )
                }) { progress -> emitProgress((baseProgress + progress / 100.0 * 35).toInt()) }
            stopwatch.stop()
            emitProgress(46)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Patch error", e)
            errorMessage.set(e.localizedMessage)
        }
        return false
    }

    private fun processFileEntry(
        apkFile: File,
        apkFilesManifest: ApkFilesManifest?,
        entry: ManifestEntry,
        isAdvanced: Boolean
    ): Array<ZipUtils.ZipEntrySource?>? {
        if (entry.isAdvanced && !isAdvanced) {
            return null
        }
        if (entry.targetPath.endsWith(Constants.FILE_SEPARATOR)) {
            if (entry.isXABA) {
                val manifestBytes = ZipUtil.unpackEntry(apkFile, entry.assetPath + ".manifest")
                val xabaBytes = ZipUtil.unpackEntry(apkFile, entry.assetPath + ".blob")
                val unpackedAssemblies = ZipUtils.unpackXABA(manifestBytes, xabaBytes)
                val list = ArrayList<ZipUtils.ZipEntrySource?>()
                unpackedAssemblies.forEach { (filename, bytes) ->
                    list.add(
                        ZipUtils.ZipEntrySource(
                            entry.targetPath + filename, bytes, entry.compression
                        )
                    )
                }
                return list.toTypedArray()
            } else if (entry.assetPath.contains("*")) {
                val path =
                    StringUtils.substringBeforeLast(entry.assetPath, Constants.FILE_SEPARATOR)
                val pattern =
                    StringUtils.substringAfterLast(entry.assetPath, Constants.FILE_SEPARATOR)
                try {
                    return if (entry.origin == 1) {
                        val list = ArrayList<ZipUtils.ZipEntrySource?>()
                        ZipUtil.iterate(apkFile) { inputStream, zipEntry ->
                            val entryPath = StringUtils.substringBeforeLast(
                                zipEntry.name, Constants.FILE_SEPARATOR
                            )
                            val filename = StringUtils.substringAfterLast(
                                zipEntry.name, Constants.FILE_SEPARATOR
                            )
                            if (entryPath == path && StringUtils.wildCardMatch(filename, pattern)) {
                                val bytes = ByteStreams.toByteArray(inputStream)
                                val source: ZipUtils.ZipEntrySource
                                source = if (entry.isXALZ) {
                                    ZipUtils.ZipEntrySource(
                                        entry.targetPath + filename, entry.compression
                                    ) {
                                        ByteArrayInputStream(
                                            ZipUtils.decompressXALZ(bytes)
                                        )
                                    }
                                } else {
                                    ZipUtils.ZipEntrySource(
                                        entry.targetPath + filename, bytes, entry.compression
                                    )
                                }
                                list.add(source)
                            }
                        }
                        list.toTypedArray()
                    } else {
                        context.assets.list(path)?.filter { filename ->
                            StringUtils.wildCardMatch(
                                filename, pattern
                            )
                        }?.map { filename ->
                            ZipUtils.ZipEntrySource(
                                entry.targetPath + filename, entry.compression
                            ) {
                                try {
                                    FileUtils.getLocalAsset(
                                        context, path + Constants.FILE_SEPARATOR + filename
                                    )
                                } catch (ignored: IOException) {
                                    null
                                }
                            }
                        }?.toTypedArray()
                    }
                } catch (ignored: IOException) {
                }
            }
            return null
        }
        val source: ZipUtils.ZipEntrySource
        if (entry.origin == 1) {
            val unpackEntryBytes = ZipUtil.unpackEntry(apkFile, entry.assetPath)
            source = if (entry.isXALZ) {
                ZipUtils.ZipEntrySource(entry.targetPath, entry.compression) {
                    ByteArrayInputStream(
                        ZipUtils.decompressXALZ(unpackEntryBytes)
                    )
                }
            } else {
                ZipUtils.ZipEntrySource(entry.targetPath, unpackEntryBytes, entry.compression)
            }
        } else {
            source = ZipUtils.ZipEntrySource(entry.targetPath, entry.compression) {
                var inputStream: InputStream? = null
                try {
                    inputStream = if (entry.isExternal) {
                        FileUtils.getLocalAsset(
                            context, apkFilesManifest!!.basePath + entry.assetPath
                        )
                    } else {
                        FileUtils.getLocalAsset(context, entry.assetPath)
                    }
                } catch (ignored: IOException) {
                }
                if (StringUtils.isNoneBlank(entry.patchCrc)) {
                    throw NotImplementedException("bs patch mode is not supported anymore.")
                }
                inputStream
            }
        }
        return arrayOf(source)
    }

    /**
     * 扫描全部兼容包，寻找匹配的版本，修改AndroidManifest.xml文件
     *
     * @param bytes     AndroidManifest.xml的字节数组
     * @param manifests 兼容包列表
     * @return 修改后的AndroidManifest.xml的字节数组
     */
    private fun modifyManifest(
        bytes: ByteArray, manifests: MutableList<ApkFilesManifest>
    ): ByteArray? {
        val packageName = AtomicReference<String?>()
        val versionName = AtomicReference<String?>()
        val versionCode = AtomicLong()
        val attrProcessLogic = { attr: AttrArgs? ->
            when {
                attr == null -> {
                }

                attr.type == NodeVisitor.TYPE_STRING -> {
                    val strObj = attr.obj as String
                    when (attr.name) {
                        "package" -> if (packageName.get() == null) {
                            packageName.set(strObj)
                            attr.obj = strObj.replace(
                                ManifestPatchConstants.APP_PACKAGE_NAME,
                                Constants.TARGET_PACKAGE_NAME
                            )
                        }

                        ManifestPatchConstants.PATTERN_VERSION_NAME -> if (versionName.get() == null) {
                            versionName.set(attr.obj as String)
                        }

                        "label" -> if (strObj.contains(ManifestPatchConstants.APP_NAME)) {
                            if (StringUtils.isBlank(Constants.PATCHED_APP_NAME)) {
                                attr.obj = context.getString(R.string.smapi_game_name)
                            } else {
                                attr.obj = Constants.PATCHED_APP_NAME!!
                            }
                            //                            return Collections.singletonList(new ManifestTagVisitor.AttrArgs(attr.ns, "requestLegacyExternalStorage", -1, NodeVisitor.TYPE_INT_BOOLEAN, true));
                        }

                        "authorities" -> {
                            if (strObj.contains(packageName.get()!!)) {
                                attr.obj = strObj.replace(
                                    packageName.get()!!, Constants.TARGET_PACKAGE_NAME
                                )
                            } else if (strObj.contains(ManifestPatchConstants.APP_PACKAGE_NAME)) {
                                attr.obj = strObj.replace(
                                    ManifestPatchConstants.APP_PACKAGE_NAME,
                                    Constants.TARGET_PACKAGE_NAME
                                )
                            }
                            if (strObj.contains(ManifestPatchConstants.PATTERN_MAIN_ACTIVITY)) {
                                if (versionCode.get() > Constants.MONO_10_VERSION_CODE) {
                                    attr.obj = strObj.replaceFirst(
                                        "\\w+\\.MainActivity".toRegex(),
                                        "crc648e5438a58262f792.SMainActivity"
                                    )
                                } else {
                                    attr.obj = strObj.replaceFirst(
                                        "\\w+\\.MainActivity".toRegex(),
                                        "md5723872fa9a204f7f942686e9ed9d0b7d.SMainActivity"
                                    )
                                }
                            }
                        }

                        "name" -> if (strObj.contains(ManifestPatchConstants.PATTERN_MAIN_ACTIVITY)) {
                            if (versionCode.get() > Constants.MONO_10_VERSION_CODE) {
                                attr.obj = strObj.replaceFirst(
                                    "\\w+\\.MainActivity".toRegex(),
                                    "crc648e5438a58262f792.SMainActivity"
                                )
                            } else {
                                attr.obj = strObj.replaceFirst(
                                    "\\w+\\.MainActivity".toRegex(),
                                    "md5723872fa9a204f7f942686e9ed9d0b7d.SMainActivity"
                                )
                            }
                        }

                        else -> {}
                    }
                }

                attr.type == NodeVisitor.TYPE_FIRST_INT -> {
                    if (StringUtils.equals(
                            attr.name, ManifestPatchConstants.PATTERN_VERSION_CODE
                        )
                    ) {
                        versionCode.set((attr.obj as Int).toLong())
                    }
                }

                attr.type == NodeVisitor.TYPE_INT_BOOLEAN -> {
                    if (StringUtils.equals(
                            attr.name, ManifestPatchConstants.PATTERN_EXTRACT_NATIVE_LIBS
                        )
                    ) {
                        attr.obj = true
                    }
                }
            }
            null
        }
        val permissionAppended = AtomicReference(true)
        val childProcessLogic = { child: ChildArgs ->
            if (!permissionAppended.get() && StringUtils.equals(child.name, "uses-permission")) {
                permissionAppended.set(true)
                listOf(
                    ChildArgs(
                        child.ns, child.name, listOf(
                            AttrArgs(
                                "http://schemas.android.com/apk/res/android",
                                "name",
                                -1,
                                NodeVisitor.TYPE_STRING,
                                "android.permission.MANAGE_EXTERNAL_STORAGE"
                            )
                        )
                    )
                )
            }
            null
        }
        return try {
            val modifyManifest =
                ManifestUtil.modifyManifest(bytes, attrProcessLogic, childProcessLogic)
            if (StringUtils.endsWith(
                    versionName.get(), ManifestPatchConstants.PATTERN_VERSION_AMAZON
                )
            ) {
                packageName.set(ManifestPatchConstants.APP_PACKAGE_NAME + ManifestPatchConstants.PATTERN_VERSION_AMAZON)
            }
            CommonLogic.filterManifest(manifests, packageName.get(), versionCode.get())
            modifyManifest
        } catch (e: Exception) {
            errorMessage.set(e.localizedMessage)
            null
        }
    }

    /**
     * 重新签名安装包
     *
     * @param apkPath APK文件路径
     * @return 签名后的安装包路径
     */
    fun sign(apkPath: String): String? {
        try {
            val stadewValleyBasePath = FileUtils.stadewValleyBasePath
            emitProgress(47)
            val signApkPath =
                stadewValleyBasePath + "/SMAPI Installer/" + FilenameUtils.getBaseName(apkPath) + "_signed.apk"
            val ks: KeyStore = JksKeyStore()
            context.assets.open("debug.keystore.dat")
                .use { fis -> ks.load(fis, PASSWORD.toCharArray()) }
            val alias = ks.aliases().nextElement()
            val publicKey = ks.getCertificate(alias) as X509Certificate
            val privateKey = ks.getKey(alias, "android".toCharArray()) as PrivateKey
            emitProgress(49)
            val outputFile = File(signApkPath)
            val engineSignerConfigs = listOf(
                DefaultApkSignerEngine.SignerConfig.Builder(
                    "debug", privateKey, listOf(publicKey)
                ).build()
            )
            val signerEngine =
                DefaultApkSignerEngine.Builder(engineSignerConfigs, 19).setV1SigningEnabled(true)
                    .setV2SigningEnabled(true).setV3SigningEnabled(false).build()
            if (originSignInfo != null && originSignInfo!!.first != null) {
                signerEngine.initWith(originSignInfo!!.first, originSignInfo!!.second)
            }
            val zipOpElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS)
            stopwatch.reset()
            val thread = Thread(Runnable {
                stopwatch.start()
                while (true) {
                    try {
                        Thread.sleep(200)
                        val elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS)
                        val progress = elapsed * 0.98 / zipOpElapsed
                        if (progress < 1.0) {
                            emitProgress((49 + 45 * progress).toInt())
                        }
                    } catch (ignored: InterruptedException) {
                        return@Runnable
                    }
                }
            })
            thread.start()
            RandomAccessFile(apkPath, "r").use { inputApkFile ->
                val signer = ApkSigner.Builder(signerEngine)
                    .setInputApk(DataSources.asDataSource(inputApkFile, 0, inputApkFile.length()))
                    .setOutputApk(outputFile).build()
                signer.sign()
            }
            org.zeroturnaround.zip.commons.FileUtils.forceDelete(File(apkPath))
            val result = ApkVerifier.Builder(outputFile).build().verify()
            if (thread.isAlive && !thread.isInterrupted) {
                thread.interrupt()
            }
            if (result.containsErrors() && result.errors.size > 0) {
                errorMessage.set(result.errors.joinToString(",") { issueWithParams -> issueWithParams.toString() })
                return null
            }
            emitProgress(95)
            return signApkPath
        } catch (e: Exception) {
            Log.e(TAG, "Sign error", e)
            errorMessage.set(e.localizedMessage)
        }
        return null
    }

    /**
     * 对指定安装包发起安装
     *
     * @param apkPath             安装包路径
     */
    fun install(apkPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val haveInstallPermission = context.packageManager.canRequestPackageInstalls()
            if (!haveInstallPermission) {
                DialogUtils.showConfirmDialog(
                    MainActivity.instance,
                    R.string.confirm,
                    R.string.request_unknown_source_permission
                ) { _, dialogAction ->
                    if (dialogAction === DialogAction.POSITIVE) {
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        intent.data = Uri.parse("package:" + context.packageName)
                        registerListener(ActivityResultHandler.REQUEST_CODE_APP_INSTALL) { _, _ ->
                            install(
                                apkPath
                            )
                        }
                        MainActivity.instance!!.startActivityForResult(
                            intent, ActivityResultHandler.REQUEST_CODE_APP_INSTALL
                        )
                    }
                }
                return
            }
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fromFile(File(apkPath)), "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Install error", e)
            errorMessage.set(e.localizedMessage)
        }
    }

    /**
     * Gets the URI from a file
     *
     * @param file = The file to try and get the URI from
     * @return The URI for the file
     */
    private fun fromFile(file: File): Uri {
        //Android versions greater than Nougat use FileProvider, others use the URI.fromFile.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider", file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    fun getGamePackageName(): String? {
        return gamePackageName.get()
    }

    fun getGameVersionCode(): Long {
        return gameVersionCode.get()
    }

    private fun emitProgress(progress: Int) {
        if (lastProgress < progress) {
            lastProgress = progress
            for (consumer in progressListener) {
                consumer.invoke(progress)
            }
        }
    }

    fun registerProgressListener(listener: (Int) -> Unit) {
        progressListener.add(listener)
    }

    companion object {
        private const val PASSWORD = "android"
        private const val TAG = "PATCHER"
    }
}