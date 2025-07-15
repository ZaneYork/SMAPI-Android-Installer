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
     * @return 抽取后的APK文件路径列表，如果抽取失败返回null
     */
    fun extract(advancedStage: Int): Tuple2<List<String>, Array<String?>>? {
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
                val splitDirs = packageInfo.applicationInfo.splitSourceDirs

                gamePackageName.set(CommonLogic.computePackageName(packageInfo))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    gameVersionCode.set(packageInfo.longVersionCode)
                } else {
                    gameVersionCode.set(packageInfo.versionCode.toLong())
                }

                val stadewValleyBasePath = FileUtils.stadewValleyBasePath
                val destDir = File("$stadewValleyBasePath/SMAPI Installer/")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }

                // 收集所有APK文件路径（主APK + 拆分APK）
                val allApkPaths = mutableListOf<String>()
                allApkPaths.add(sourceDir)
                splitDirs?.forEach { if (it != null) allApkPaths.add(it) }

                // 将所有APK文件复制到目标目录
                allApkPaths.forEachIndexed { index, apkPath ->
                    val srcFile = File(apkPath)
                    val destFile = File(destDir, "base_${index}.apk")
                    srcFile.copyTo(destFile, overwrite = true)
                    emitProgress(10 + (index * 80 / allApkPaths.size))
                }

                // 返回复制后的APK文件路径列表
                val copiedApkPaths = allApkPaths.mapIndexed { index, _ ->
                    File(destDir, "base_${index}.apk").absolutePath
                }

                if (advancedStage == 0) {
                    // 初始化阶段提取资源
                    val count = AtomicInteger()
                    ZipUtil.unpack(
                        File(sourceDir), File("$stadewValleyBasePath/StardewValley/")
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
                } else if (advancedStage == 1) {
                    // 高级安装阶段确保资源存在
                    val contentFolder = File("$stadewValleyBasePath/StardewValley/Content")
                    if (!contentFolder.exists()) {
                        extract(0)
                    }
                }

                Tuple2(copiedApkPaths, arrayOfNulls(0))
            } catch (ignored: PackageManager.NameNotFoundException) {
                null
            }
        }
        errorMessage.set(context.getString(R.string.error_game_not_found))
        return null
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