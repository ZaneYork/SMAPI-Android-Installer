package com.zane.smapiinstaller.ui.install;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.io.Files;
import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.AppConfigKeyConstants;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.FragmentInstallBinding;
import com.zane.smapiinstaller.dto.Tuple2;
import com.zane.smapiinstaller.logic.ActivityResultHandler;
import com.zane.smapiinstaller.logic.ApkPatcher;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.ui.main.MainTabsFragmentDirections;
import com.zane.smapiinstaller.utils.ConfigUtils;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

/**
 * @author Zane
 */
public class InstallFragment extends Fragment {

    private Activity context;

    private Thread task;

    private FragmentInstallBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInstallBinding.inflate(inflater, container, false);
        context = this.getActivity();
        if (Boolean.parseBoolean(ConfigUtils.getConfig((MainApplication) context.getApplication(), AppConfigKeyConstants.ADVANCED_MODE, "false").getValue())) {
            binding.buttonInstall.setVisibility(View.INVISIBLE);
            binding.layoutAdvInstall.setVisibility(View.VISIBLE);
        }
        try {
            String firstLine = Files.asCharSource(FileUtils.docOverlayFetch(context, Constants.LOG_PATH), StandardCharsets.UTF_8).readFirstLine();
            if (StringUtils.isNoneBlank(firstLine)) {
                String versionString = RegExUtils.removePattern(firstLine, "\\[.+\\]\\s+");
                versionString = RegExUtils.removePattern(versionString, "\\s+with.+");
                binding.textLatestRunning.setText(context.getString(R.string.smapi_version_runing, versionString));
                binding.textLatestRunning.setVisibility(View.VISIBLE);
            }
        } catch (IOException ignored) {
        }
        binding.buttonInstall.setOnClickListener(v -> InstallFragment.this.install());
        binding.buttonAdvInitial.setOnClickListener(v -> InstallFragment.this.initialLogic());
        binding.buttonAdvInstall.setOnClickListener(v -> InstallFragment.this.installLogic(true));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DialogUtils.showConfirmDialog(binding.getRoot(), R.string.confirm, R.string.android_version_confirm, ((dialog, which) -> {
                if (which == DialogAction.POSITIVE) {
                    installLogic(false);
                }
            }));
        } else {
            installLogic(false);
        }
    }

    /**
     * 初始化逻辑
     */
    private void initialLogic() {
        if (task != null) {
            task.interrupt();
        }
        task = new Thread(() -> CommonLogic.showProgressDialog(binding.getRoot(), context, (dialog) -> {
            ApkPatcher patcher = new ApkPatcher(context);
            patcher.registerProgressListener((progress) -> DialogUtils.setProgressDialogState(binding.getRoot(), dialog, null, progress));
            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.extracting_package, null);
            Tuple2<String, String[]> paths = patcher.extract(0);
            if (paths == null) {
                DialogUtils.showAlertDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.error_game_not_found)));
            }
        }));
        task.start();
    }

    /**
     * 安装逻辑
     */
    private void installLogic(boolean isAdv) {
        if (!CommonLogic.requestDataRootPermission(context, ActivityResultHandler.REQUEST_CODE_DATA_FILES_ACCESS_PERMISSION, (success) -> installLogic(isAdv))) {
            return;
        }
        if (task != null) {
            task.interrupt();
        }
        task = new Thread(() -> CommonLogic.showProgressDialog(binding.getRoot(), context, (dialog) -> {
            ApkPatcher patcher = new ApkPatcher(context);
            patcher.registerProgressListener((progress) -> DialogUtils.setProgressDialogState(binding.getRoot(), dialog, null, progress));
            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.extracting_package, null);
            Tuple2<String, String[]> paths = patcher.extract(isAdv ? 1 : -1);
            String stadewValleyBasePath = FileUtils.getStadewValleyBasePath();
            File dest = new File(stadewValleyBasePath + "/SMAPI Installer/");
            boolean failed = paths == null;
            if (!dest.exists()) {
                if (!dest.mkdir()) {
                    failed = true;
                }
            }
            if (failed) {
                DialogUtils.showAlertDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.error_game_not_found)));
                return;
            }
            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.patching_package, 8);
            File targetApk = new File(dest, "base.apk");
            if (!patcher.patch(paths.getFirst(), paths.getSecond(), targetApk, isAdv, false)) {
                int target = patcher.getSwitchAction().getAndSet(0);
                if (target == R.string.menu_download) {
                    DialogUtils.showConfirmDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_patch_game)), R.string.menu_download, R.string.cancel, (d, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            NavController controller = Navigation.findNavController(binding.buttonInstall);
                            controller.navigate(MainTabsFragmentDirections.actionNavMainToNavDownload());
                        }
                    });
                } else {
                    DialogUtils.showAlertDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_patch_game)));
                }
                return;
            }
            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.unpacking_smapi_files, null);
            if (!CommonLogic.unpackSmapiFiles(context, targetApk.getAbsolutePath(), false, patcher.getGamePackageName(), patcher.getGameVersionCode())) {
                DialogUtils.showAlertDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_unpack_smapi_files)));
                return;
            }
            ModAssetsManager modAssetsManager = new ModAssetsManager(binding.getRoot());
            modAssetsManager.installDefaultMods();

            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.signing_package, null);
            String signPath = patcher.sign(targetApk.getAbsolutePath());
            if (signPath == null) {
                DialogUtils.showAlertDialog(binding.getRoot(), R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_sign_game)));
                return;
            }
            DialogUtils.setProgressDialogState(binding.getRoot(), dialog, R.string.installing_package, null);
            patcher.install(signPath);
        }));
        task.start();
    }
}
