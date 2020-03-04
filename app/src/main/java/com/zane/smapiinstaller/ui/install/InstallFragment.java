package com.zane.smapiinstaller.ui.install;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.logic.ApkPatcher;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InstallFragment extends Fragment {

    private Context context;

    private Thread task;

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_install, container, false);
        ButterKnife.bind(this, root);
        context = this.getActivity();
        return root;
    }

    @OnClick(R.id.button_install)
    void Install() {
        new MaterialDialog.Builder(context).title(R.string.install_progress_title).content(R.string.extracting_package).contentGravity(GravityEnum.CENTER)
                .progress(false, 100, true).cancelable(false).cancelListener(dialog -> {
            if (task != null) {
                task.interrupt();
            }
        }).showListener(dialogInterface -> {
            final MaterialDialog dialog = (MaterialDialog) dialogInterface;
            if (task != null) {
                task.interrupt();
            }
            task = new Thread(() -> {
                try {
                    ApkPatcher patcher = new ApkPatcher(context);
                    CommonLogic.setProgressDialogState(root, dialog, R.string.extracting_package, 0);
                    String path = patcher.extract();
                    if (path == null) {
                        CommonLogic.showAlertDialog(root, R.string.error, R.string.error_game_not_found);
                        return;
                    }
                    CommonLogic.setProgressDialogState(root, dialog, R.string.unpacking_smapi_files, 10);
                    if (!CommonLogic.unpackSmapiFiles(context, path, false)) {
                        CommonLogic.showAlertDialog(root, R.string.error, R.string.failed_to_unpack_smapi_files);
                        return;
                    }
                    ModAssetsManager modAssetsManager = new ModAssetsManager(root);
                    CommonLogic.setProgressDialogState(root, dialog, R.string.unpacking_smapi_files, 15);
                    modAssetsManager.installDefaultMods();
                    CommonLogic.setProgressDialogState(root, dialog, R.string.patching_package, 25);
                    if (!patcher.patch(path)) {
                        CommonLogic.showAlertDialog(root, R.string.error, R.string.failed_to_patch_game);
                        return;
                    }
                    CommonLogic.setProgressDialogState(root, dialog, R.string.signing_package, 55);
                    String signPath = patcher.sign(path);
                    if (signPath == null) {
                        CommonLogic.showAlertDialog(root, R.string.error, R.string.failed_to_sign_game);
                        return;
                    }
                    CommonLogic.setProgressDialogState(root, dialog, R.string.installing_package, 99);
                    patcher.install(signPath);
                    dialog.incrementProgress(1);

                } finally {
                    if (!dialog.isCancelled()) {
                        dialog.dismiss();
                    }
                }
            });
            task.start();
        }).show();
    }

}
