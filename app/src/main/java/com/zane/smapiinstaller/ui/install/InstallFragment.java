package com.zane.smapiinstaller.ui.install;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.logic.ApkPatcher;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.DialogUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.SneakyThrows;

public class InstallFragment extends Fragment {

    private Context context;

    private Thread task;

    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_install, container, false);
        ButterKnife.bind(this, root);
        context = this.getActivity();
        return root;
    }

    @OnClick(R.id.button_install)
    void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DialogUtils.showConfirmDialog(root, R.string.confirm, R.string.android_version_confirm, ((dialog, which) -> {
                if (which == DialogAction.POSITIVE) {
                    installLogic();
                }
            }));
        } else {
            installLogic();
        }
    }

    /**
     * 安装逻辑
     */
    private void installLogic() {
        new MaterialDialog.Builder(context).title(R.string.install_progress_title).content(R.string.extracting_package).contentGravity(GravityEnum.CENTER)
                .progress(false, 100, true).cancelable(false).cancelListener(dialog -> {
            if (task != null) {
                task.interrupt();
            }
        }).showListener(dialogInterface -> {
            final MaterialDialog dialog = (MaterialDialog) dialogInterface;
            DialogUtils.setCurrentDialog(dialog);
            if (task != null) {
                task.interrupt();
            }
            task = new Thread(() -> {
                try {
                    ApkPatcher patcher = new ApkPatcher(context);
                    patcher.registerProgressListener((progress)->{
                        DialogUtils.setProgressDialogState(root, dialog, null, progress);
                    });
                    DialogUtils.setProgressDialogState(root, dialog, R.string.extracting_package, null);
                    String path = patcher.extract();
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
                    if (!patcher.patch(path)) {
                        int target = patcher.getSwitchAction().getAndSet(0);
                        if(target == R.string.menu_download) {
                            DialogUtils.showConfirmDialog(root, R.string.error, StringUtils.firstNonBlank(patcher.getErrorMessage().get(), context.getString(R.string.failed_to_patch_game)), R.string.menu_download, R.string.cancel, (d, which) -> {
                                if(which == DialogAction.POSITIVE) {
                                    NavController controller = Navigation.findNavController(root);
                                    controller.navigate(InstallFragmentDirections.actionNavInstallToNavDownload());
                                }
                            });
                        }
                        else {
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
                }
                catch (Exception e) {
                    Crashes.trackError(e);
                    DialogUtils.showAlertDialog(root, R.string.error, e.getLocalizedMessage());
                }
                finally {
                    DialogUtils.dismissDialog(root, dialog);
                }
            });
            task.start();
        }).show();
    }

}
