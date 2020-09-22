package com.zane.smapiinstaller.ui.install;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.io.Files;
import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.AppConfigKeyConstants;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.logic.ApkPatcher;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.ui.main.MainTabsFragmentDirections;
import com.zane.smapiinstaller.utils.ConfigUtils;
import com.zane.smapiinstaller.utils.DialogUtils;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Zane
 */
public class InstallFragment extends Fragment {

    private Activity context;

    private Thread task;

    private View root;

    @BindView(R.id.button_install)
    Button installButton;

    @BindView(R.id.text_latest_running)
    TextView textLatestRunning;

    @BindView(R.id.layout_adv_install)
    LinearLayout layoutAdvInstall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_install, container, false);
        ButterKnife.bind(this, root);
        context = this.getActivity();
        if (Boolean.parseBoolean(ConfigUtils.getConfig((MainApplication) context.getApplication(), AppConfigKeyConstants.ADVANCED_MODE, "false").getValue())) {
            installButton.setVisibility(View.INVISIBLE);
            layoutAdvInstall.setVisibility(View.VISIBLE);
        }
        try {
            String firstLine = Files.asCharSource(new File(Environment.getExternalStorageDirectory(), Constants.LOG_PATH), StandardCharsets.UTF_8).readFirstLine();
            if (StringUtils.isNoneBlank(firstLine)) {
                String versionString = RegExUtils.removePattern(firstLine, "\\[.+\\]\\s+");
                versionString = RegExUtils.removePattern(versionString, "\\s+with.+");
                textLatestRunning.setText(context.getString(R.string.smapi_version_runing, versionString));
                textLatestRunning.setVisibility(View.VISIBLE);
            }
        } catch (IOException ignored) {
        }
        return root;
    }

    @OnClick(R.id.button_install)
    void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DialogUtils.showConfirmDialog(root, R.string.confirm, R.string.android_version_confirm, ((dialog, which) -> {
                if (which == DialogAction.POSITIVE) {
                    installLogic(false);
                }
            }));
        } else {
            installLogic(false);
        }
    }

    @OnClick(R.id.button_adv_initial)
    void advInitial() {
        initialLogic();
    }

    @OnClick(R.id.button_adv_install)
    void advInstall() {
        installLogic(true);
    }

    /**
     * 初始化逻辑
     */
    private void initialLogic() {
        if (task != null) {
            task.interrupt();
        }
        task = new Thread(() -> CommonLogic.showProgressDialog(root, context, (dialog)->{
            ApkPatcher patcher = new ApkPatcher(context);
            patcher.registerProgressListener((progress) -> DialogUtils.setProgressDialogState(root, dialog, null, progress));
            DialogUtils.setProgressDialogState(root, dialog, R.string.extracting_package, null);
            String path = patcher.extract(0);
            if (path == null) {
                DialogUtils.showAlertDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.error_game_not_found)));
            }
        }));
        task.start();
    }

    /**
     * 安装逻辑
     */
    private void installLogic(boolean isAdv) {
        if (task != null) {
            task.interrupt();
        }
        task = new Thread(() -> CommonLogic.showProgressDialog(root, context, (dialog)-> {
            ApkPatcher patcher = new ApkPatcher(context);
            patcher.registerProgressListener((progress) -> DialogUtils.setProgressDialogState(root, dialog, null, progress));
            DialogUtils.setProgressDialogState(root, dialog, R.string.extracting_package, null);
            String path = patcher.extract(isAdv ? 1 : -1);
            if (path == null) {
                DialogUtils.showAlertDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.error_game_not_found)));
                return;
            }
            DialogUtils.setProgressDialogState(root, dialog, R.string.unpacking_smapi_files, null);
            if (!CommonLogic.unpackSmapiFiles(context, path, false)) {
                DialogUtils.showAlertDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_unpack_smapi_files)));
                return;
            }
            ModAssetsManager modAssetsManager = new ModAssetsManager(root);
            DialogUtils.setProgressDialogState(root, dialog, R.string.unpacking_smapi_files, 6);
            modAssetsManager.installDefaultMods();
            DialogUtils.setProgressDialogState(root, dialog, R.string.patching_package, 8);
            if (!patcher.patch(path, isAdv)) {
                int target = patcher.getSwitchAction().getAndSet(0);
                if (target == R.string.menu_download) {
                    DialogUtils.showConfirmDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_patch_game)), R.string.menu_download, R.string.cancel, (d, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            NavController controller = Navigation.findNavController(installButton);
                            controller.navigate(MainTabsFragmentDirections.actionNavMainToNavDownload());
                        }
                    });
                } else {
                    DialogUtils.showAlertDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_patch_game)));
                }
                return;
            }
            DialogUtils.setProgressDialogState(root, dialog, R.string.signing_package, null);
            String signPath = patcher.sign(path);
            if (signPath == null) {
                DialogUtils.showAlertDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_sign_game)));
                return;
            }
            DialogUtils.setProgressDialogState(root, dialog, R.string.installing_package, null);
            patcher.install(signPath);
        }));
        task.start();
    }

}
