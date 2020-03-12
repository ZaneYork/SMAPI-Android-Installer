package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;

public class GameLauncher {

    private final View root;

    public GameLauncher(View root) {
        this.root = root;
    }

    public void launch() {
        Activity context = CommonLogic.getActivityFromView(root);
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME, 0);
            if(!CommonLogic.unpackSmapiFiles(context, packageInfo.applicationInfo.publicSourceDir, true)) {
                CommonLogic.showAlertDialog(root, R.string.error, R.string.error_failed_to_repair);
                return;
            }
            ModAssetsManager modAssetsManager = new ModAssetsManager(root);
            modAssetsManager.checkModEnvironment((isConfirm) -> {
                if(isConfirm) {
                    Intent intent = packageManager.getLaunchIntentForPackage(Constants.TARGET_PACKAGE_NAME);
                    context.startActivity(intent);
                }
            });
        } catch (PackageManager.NameNotFoundException ignored) {
            CommonLogic.showAlertDialog(root, R.string.error, R.string.error_smapi_not_installed);
        } catch (Exception e) {
            Crashes.trackError(e);
            CommonLogic.showAlertDialog(root, R.string.error, e.getLocalizedMessage());
        }
    }
}
