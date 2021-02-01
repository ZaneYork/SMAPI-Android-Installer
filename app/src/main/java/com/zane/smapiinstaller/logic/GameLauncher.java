package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.ManifestPatchConstants;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.StringUtils;

/**
 * 游戏启动器
 * @author Zane
 */
public class GameLauncher {

    private final View root;

    public GameLauncher(View root) {
        this.root = root;
    }

    /**
     * 检查已安装MOD版本游戏
     * @param context 上下文
     * @return 软件包信息
     */
    public static PackageInfo getGamePackageInfo(Activity context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo;
            try {
                packageInfo = packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
                packageInfo = packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME_SAMSUNG, 0);
            }
            return packageInfo;
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    /**
     * 启动逻辑
     */
    public void launch() {
        Activity context = CommonLogic.getActivityFromView(root);
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = getGamePackageInfo(context);
            if(packageInfo == null) {
                DialogUtils.showAlertDialog(root, R.string.error, R.string.error_smapi_not_installed);
                return;
            }
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            }
            else {
                versionCode = packageInfo.versionCode;
            }
            if(!CommonLogic.unpackSmapiFiles(context, packageInfo.applicationInfo.publicSourceDir, true, CommonLogic.computePackageName(packageInfo), versionCode)) {
                DialogUtils.showAlertDialog(root, R.string.error, R.string.error_failed_to_repair);
                return;
            }
            ModAssetsManager modAssetsManager = new ModAssetsManager(root);
            modAssetsManager.checkModEnvironment((isConfirm) -> {
                if(isConfirm) {
                    Intent intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName);
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            Crashes.trackError(e);
            DialogUtils.showAlertDialog(root, R.string.error, e.getLocalizedMessage());
        }
    }
}
