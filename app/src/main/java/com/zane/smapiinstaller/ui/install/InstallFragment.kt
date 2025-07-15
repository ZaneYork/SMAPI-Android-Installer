package com.zane.smapiinstaller.ui.install

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.common.io.Files
import com.reandroid.apk.ApkBundle
import com.reandroid.apk.ApkModule
import com.reandroid.apkeditor.common.AndroidManifestHelper
import com.reandroid.apkeditor.compile.BuildOptions
import com.reandroid.apkeditor.decompile.DecompileOptions
import com.reandroid.apkeditor.decompile.Decompiler
import com.reandroid.app.AndroidManifest
import com.zane.smapiinstaller.MainApplication
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.AppConfigKeyConstants
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.databinding.FragmentInstallBinding
import com.zane.smapiinstaller.logic.ActivityResultHandler
import com.zane.smapiinstaller.logic.ApkPatcher

import com.zane.smapiinstaller.logic.CommonLogic.requestDataRootPermission
import com.zane.smapiinstaller.logic.CommonLogic.showProgressDialog
import com.zane.smapiinstaller.logic.CommonLogic.unpackSmapiFiles
import com.zane.smapiinstaller.logic.ModAssetsManager
import com.zane.smapiinstaller.utils.ConfigUtils
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.ZipUtils
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author Zane
 */
class InstallFragment : Fragment() {
    private var task: Thread? = null
    private lateinit var binding: FragmentInstallBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentInstallBinding.inflate(inflater, container, false)
        if (java.lang.Boolean.parseBoolean(
                ConfigUtils.getConfig(
                    requireContext().applicationContext as MainApplication,
                    AppConfigKeyConstants.ADVANCED_MODE,
                    "false"
                ).value
            )
        ) {
            binding.buttonInstall.visibility = View.INVISIBLE
            binding.layoutAdvInstall.visibility = View.VISIBLE
        }
        try {
            val firstLine =
                context?.let { FileUtils.docOverlayFetch(it, Constants.LOG_PATH) }?.let {
                    Files.asCharSource(
                        it, StandardCharsets.UTF_8
                    ).readFirstLine()
                }
            if (StringUtils.isNoneBlank(firstLine)) {
                var versionString = RegExUtils.removePattern(firstLine, "\\[.+\\]\\s+")
                versionString = RegExUtils.removePattern(versionString, "\\s+with.+")
                binding.textLatestRunning.text =
                    requireContext().getString(R.string.smapi_version_runing, versionString)
                binding.textLatestRunning.visibility = View.VISIBLE
            }
        } catch (ignored: IOException) {
        }
        binding.buttonInstall.setOnClickListener { install() }
        binding.buttonAdvInitial.setOnClickListener { initialLogic() }
        binding.buttonAdvInstall.setOnClickListener { installLogic(true) }
        return binding.root
    }

    private fun install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DialogUtils.showConfirmDialog(
                binding.root, R.string.confirm, R.string.android_version_confirm
            ) { _, which ->
                if (which === DialogAction.POSITIVE) {
                    installLogic(false)
                }
            }
        } else {
            installLogic(false)
        }
    }

    /**
     * 初始化逻辑
     */
    private fun initialLogic() {
        task?.interrupt()
        task = Thread {
            showProgressDialog(binding.root, requireContext()) { dialog ->
                val patcher = ApkPatcher(
                    requireContext()
                )
                patcher.registerProgressListener { progress ->
                    DialogUtils.setProgressDialogState(
                        binding.root, dialog, null, progress
                    )
                }
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.extracting_package, null
                )
                val paths = patcher.extract(0)
                if (paths == null) {
                    DialogUtils.showAlertDialog(
                        binding.root, R.string.error, StringUtils.firstNonBlank(
                            patcher.errorMessage.get(),
                            requireContext().getString(R.string.error_game_not_found)
                        )
                    )
                }
            }
        }
        task?.start()
    }

    /**
     * 安装逻辑
     */
    private fun installLogic(isAdv: Boolean) {
        if (!requestDataRootPermission(
                requireActivity(), ActivityResultHandler.REQUEST_CODE_DATA_FILES_ACCESS_PERMISSION
            ) { installLogic(isAdv) }
        ) {
            return
        }
        task?.interrupt()
        task = Thread {
            showProgressDialog(binding.root, requireContext()) { dialog ->
                val patcher = ApkPatcher(
                    requireContext()
                )
                patcher.registerProgressListener { progress ->
                    DialogUtils.setProgressDialogState(
                        binding.root, dialog, null, progress
                    )
                }
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.extracting_package, null
                )

                val paths = patcher.extract(if (isAdv) 1 else -1)
                val stadewValleyBasePath = FileUtils.stadewValleyBasePath
                val dest = File("$stadewValleyBasePath/SMAPI Installer/")
                var failed = paths == null
                if (!dest.exists()) {
                    if (!dest.mkdir()) {
                        failed = true
                    }
                }
                if (failed) {
                    DialogUtils.showAlertDialog(
                        binding.root, R.string.error, StringUtils.firstNonBlank(
                            patcher.errorMessage.get(),
                            requireContext().getString(R.string.error_game_not_found)
                        )
                    )
                    return@showProgressDialog
                }

                // 1. 创建反编译目录
                val decompiledDir = File(dest, "decompiled_merged")
                if (decompiledDir.exists()) {
                    decompiledDir.deleteRecursively()
                }
                decompiledDir.mkdirs()

                // 2. 解压apk.zip到反编译目录
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.unpacking_smapi_project, null
                )
                if (!unpackSmapiProject(requireContext(), decompiledDir)) {
                    DialogUtils.showAlertDialog(
                        binding.root,
                        R.string.error,
                        requireContext().getString(R.string.failed_to_unpack_smapi_project)
                    )
                    return@showProgressDialog
                }

                // 3. 从base_0.apk提取程序集
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.extracting_assemblies, null
                )
                val base0Apk = paths!!.first.find { it.endsWith("base_0.apk") }
                if (base0Apk == null) {
                    DialogUtils.showAlertDialog(
                        binding.root,
                        R.string.error,
                        requireContext().getString(R.string.error_base0_not_found)
                    )
                    return@showProgressDialog
                }
                val assembliesDir = File(decompiledDir, "root/assemblies")
                if (!assembliesDir.exists()) {
                    assembliesDir.mkdirs()
                }
                if (!extractDllsFromBaseApk(File(base0Apk), assembliesDir)) {
                    DialogUtils.showAlertDialog(
                        binding.root,
                        R.string.error,
                        requireContext().getString(R.string.failed_to_extract_dlls)
                    )
                    return@showProgressDialog
                }

                // 4. 重新打包APK
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.repackaging_apk, null
                )
                val targetApk = File(dest, "base.apk")
                if (!repackApk(decompiledDir, targetApk)) {
                    DialogUtils.showAlertDialog(
                        binding.root,
                        R.string.error,
                        requireContext().getString(R.string.failed_to_repack)
                    )
                    return@showProgressDialog
                }

                // 5. 提取游戏内容和SMAPI文件
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.extracting_game_content, null
                )

                // 5.1 解压base_2.apk的assets/Content到StardewValley/Content
                val base2Apk = paths.first.find { it.endsWith("base_2.apk") }
                if (base2Apk != null) {
                    extractContentFromBase2(File(base2Apk), stadewValleyBasePath)
                } else {
                    Log.w("SMAPI", "base_2.apk not found, skipping content extraction")
                }

                // 5.2 解压assets/smapi文件夹到smapi-internal
                extractSmapiFiles(requireContext(), stadewValleyBasePath)

                // 5.3 复制程序集到smapi-internal
                copyAssembliesToInternal(assembliesDir, stadewValleyBasePath)

                // 6. 签名APK
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.signing_package, null
                )
                val signPath = patcher.sign(targetApk.absolutePath)
                if (signPath == null) {
                    DialogUtils.showAlertDialog(
                        binding.root, R.string.error, StringUtils.firstNonBlank(
                            patcher.errorMessage.get(),
                            requireContext().getString(R.string.failed_to_sign_game)
                        )
                    )
                    return@showProgressDialog
                }

                val targetApksign = File(dest, "base_signed.apk")
                val targetbase_0 = File(dest, "base_0.apk")
                val targetbase_1 = File(dest, "base_1.apk")
                val targetbase_2 = File(dest, "base_2.apk")
                // 7. 安装APK
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.installing_package, null
                )
                patcher.install(targetApksign.absolutePath)

                //targetApksign.delete();
                targetbase_0.delete()
                targetbase_1.delete()
                targetbase_2.delete()

            }
        }
        task?.start()
    }

    /**
     * 从base_2.apk提取assets/Content到StardewValley/Content
     */
    private fun extractContentFromBase2(base2Apk: File, basePath: String) {
        val contentDir = File("$basePath/StardewValley/Content")
        if (!contentDir.exists()) {
            contentDir.mkdirs()
        }

        var totalFiles = 0
        var extractedFiles = 0
        var lastProgressLog = System.currentTimeMillis()
        val buffer = ByteArray(8192)

        try {
            FileInputStream(base2Apk).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        totalFiles++

                        if (entry.name.startsWith("assets/Content/")) {
                            // 计算目标路径（移除"assets/Content/"前缀）
                            val relativePath = entry.name.substring("assets/Content/".length)
                            val targetFile = File(contentDir, relativePath)

                            // 确保目录存在
                            targetFile.parentFile?.mkdirs()

                            if (!entry.isDirectory) {
                                // 解压文件
                                FileOutputStream(targetFile).use { fos ->
                                    BufferedOutputStream(fos).use { bos ->
                                        var len: Int
                                        while (zis.read(buffer).also { len = it } != -1) {
                                            bos.write(buffer, 0, len)
                                        }
                                    }
                                }
                                extractedFiles++

                                // 每50个文件或每秒记录一次进度
                                val now = System.currentTimeMillis()
                                if (extractedFiles % 50 == 0 || now - lastProgressLog > 1000) {
                                    Log.d("SMAPI", "Extracted $extractedFiles content files")
                                    lastProgressLog = now
                                }
                            } else {
                                // 如果是目录，确保创建
                                if (!targetFile.exists()) {
                                    targetFile.mkdirs()
                                }
                            }
                        }

                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            Log.i("SMAPI", "Successfully extracted $extractedFiles/$totalFiles content files")
        } catch (e: Exception) {
            Log.e("SMAPI", "Failed to extract content from base_2.apk", e)
        }
    }
    /**
     * 解压assets/smapi文件夹到smapi-internal
     */
    private fun extractSmapiFiles(context: Context, basePath: String) {
        try {
            val smapiInternalDir = File("$basePath/smapi-internal")
            if (!smapiInternalDir.exists()) {
                smapiInternalDir.mkdirs()
            }

            // 从assets获取smapi.zip
            val assetManager = context.assets
            val inputStream = assetManager.open("smapi.zip")
            ZipUtil.unpack(inputStream, smapiInternalDir)
        } catch (e: Exception) {
            Log.e("SMAPI", "Failed to extract SMAPI files", e)
        }
    }

    /**
     * 复制程序集到smapi-internal
     */
    private fun copyAssembliesToInternal(assembliesDir: File, basePath: String) {
        try {
            val targetDir = File("$basePath/smapi-internal")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            assembliesDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".dll")) {
                    val targetFile = File(targetDir, file.name)
                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    file.copyTo(targetFile)
                }
            }
        } catch (e: Exception) {
            Log.e("SMAPI", "Failed to copy assemblies to smapi-internal", e)
        }
    }

    /**
     * 解压SMAPI项目到反编译目录
     */
    private fun unpackSmapiProject(context: Context, targetDir: File): Boolean {
        return try {
            // 从assets获取apk.zip
            val assetManager = context.assets
            val inputStream = assetManager.open("apk.zip")
            ZipUtil.unpack(inputStream, targetDir)
            true
        } catch (e: Exception) {
            Log.e("SMAPI", "Failed to unpack SMAPI project", e)
            false
        }
    }

    /**
     * 从base APK提取指定DLL文件
     */
    private fun extractDllsFromBaseApk(baseApk: File, targetDir: File): Boolean {
        return try {
            // 临时目录用于提取blob文件
            val tempDir = File.createTempFile("dll_extract", null)
            tempDir.delete()
            tempDir.mkdirs()

            // 提取manifest和blob文件
            val manifestEntry = "assemblies/assemblies.manifest"
            val blobEntry = "assemblies/assemblies.blob"

            val manifestFile = File(tempDir, "assemblies.manifest")
            val blobFile = File(tempDir, "assemblies.blob")

            ZipUtil.unpackEntry(baseApk, manifestEntry, manifestFile)
            ZipUtil.unpackEntry(baseApk, blobEntry, blobFile)

            if (!manifestFile.exists() || !blobFile.exists()) {
                return false
            }

            // 解压所有DLL
            val dlls = ZipUtils.unpackXABA(manifestFile.readBytes(), blobFile.readBytes())

            // 保存需要的DLL文件
            val requiredDlls = listOf(
                "MonoGame.Framework.dll",
                "StardewValley.dll",
                "StardewValley.GameData.dll"
            )

            requiredDlls.forEach { dllName ->
                dlls[dllName]?.let { bytes ->
                    File(targetDir, dllName).writeBytes(bytes)
                }
            }

            // 清理临时文件
            tempDir.deleteRecursively()

            true
        } catch (e: Exception) {
            Log.e("SMAPI", "Failed to extract DLLs", e)
            false
        }
    }


    // 重新打包APK
    private fun repackApk(decompiledDir: File, outputApk: File): Boolean {
        return try {
            val buildOptions = BuildOptions().apply {
                inputFile = decompiledDir
                outputFile = outputApk
                type = "xml" // 使用XML格式重新打包
            }
            com.reandroid.apkeditor.compile.Builder(buildOptions).runCommand()
            true
        } catch (e: Exception) {
            false
        }
    }
}