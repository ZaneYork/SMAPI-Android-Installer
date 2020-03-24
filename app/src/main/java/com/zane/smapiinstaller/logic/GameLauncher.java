package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.utils.DialogUtils;

/**
 * 游戏启动器
 */
public class GameLauncher {

    private final View root;

    public GameLauncher(View root) {
        this.root = root;
    }

    /**
     * 启动逻辑
     */
    public void launch() {
        Activity context = CommonLogic.getActivityFromView(root);
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo;
            try {
                packageInfo = packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
                packageInfo = packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME_SAMSUNG, 0);
            }
            if(!CommonLogic.unpackSmapiFiles(context, packageInfo.applicationInfo.publicSourceDir, true)) {
                DialogUtils.showAlertDialog(root, R.string.error, R.string.error_failed_to_repair);
                return;
            }
            ModAssetsManager modAssetsManager = new ModAssetsManager(root);
            PackageInfo finalPackageInfo = packageInfo;
            modAssetsManager.checkModEnvironment((isConfirm) -> {
                if(isConfirm) {
                    Intent intent = packageManager.getLaunchIntentForPackage(finalPackageInfo.packageName);
                    context.startActivity(intent);
                }
            });
        } catch (PackageManager.NameNotFoundException ignored) {
            DialogUtils.showAlertDialog(root, R.string.error, R.string.error_smapi_not_installed);
        } catch (Exception e) {
            Crashes.trackError(e);
            DialogUtils.showAlertDialog(root, R.string.error, e.getLocalizedMessage());
        }
    }
}
