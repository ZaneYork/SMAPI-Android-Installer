package com.zane.smapiinstaller.ui.install

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.google.common.io.Files
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
import com.zane.smapiinstaller.ui.main.MainTabsFragmentDirections
import com.zane.smapiinstaller.utils.ConfigUtils
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

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
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.patching_package, 8
                )
                val targetApk = File(dest, "base.apk")
                if (!patcher.patch(paths!!.first, paths.second, targetApk, isAdv, false)) {
                    val target = patcher.switchAction.getAndSet(0)
                    if (target == R.string.menu_download) {
                        DialogUtils.showConfirmDialog(
                            binding.root, R.string.error, StringUtils.firstNonBlank(
                                patcher.errorMessage.get(),
                                requireContext().getString(R.string.failed_to_patch_game)
                            ), R.string.menu_download, R.string.cancel
                        ) { _, which ->
                            if (which === DialogAction.POSITIVE) {
                                val controller = findNavController(binding.buttonInstall)
                                controller.navigate(MainTabsFragmentDirections.actionNavMainToNavDownload())
                            }
                        }
                    } else {
                        DialogUtils.showAlertDialog(
                            binding.root, R.string.error, StringUtils.firstNonBlank(
                                patcher.errorMessage.get(),
                                requireContext().getString(R.string.failed_to_patch_game)
                            )
                        )
                    }
                    return@showProgressDialog
                }
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.unpacking_smapi_files, null
                )
                if (!unpackSmapiFiles(
                        requireActivity(),
                        targetApk.absolutePath,
                        false,
                        patcher.getGamePackageName(),
                        patcher.getGameVersionCode()
                    )
                ) {
                    DialogUtils.showAlertDialog(
                        binding.root, R.string.error, StringUtils.firstNonBlank(
                            patcher.errorMessage.get(),
                            requireContext().getString(R.string.failed_to_unpack_smapi_files)
                        )
                    )
                    return@showProgressDialog
                }
                val modAssetsManager = ModAssetsManager(binding.root)
                modAssetsManager.installDefaultMods()
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
                DialogUtils.setProgressDialogState(
                    binding.root, dialog, R.string.installing_package, null
                )
                patcher.install(signPath)
            }
        }
        task?.start()
    }
}