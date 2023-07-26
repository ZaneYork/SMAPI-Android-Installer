package com.zane.smapiinstaller.logic

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.logic.CommonLogic.computePackageName
import com.zane.smapiinstaller.logic.CommonLogic.getActivityFromView
import com.zane.smapiinstaller.logic.CommonLogic.unpackSmapiFiles
import com.zane.smapiinstaller.utils.DialogUtils.showAlertDialog

/**
 * 游戏启动器
 * @author Zane
 */
class GameLauncher(private val root: View) {
    /**
     * 启动逻辑
     */
    fun launch() {
        val context = getActivityFromView(root) ?: return
        val packageManager = context.packageManager
        try {
            val packageInfo = getGamePackageInfo(context)
            if (packageInfo == null) {
                showAlertDialog(root, R.string.error, R.string.error_smapi_not_installed)
                return
            }
            val versionCode: Long
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            if (!unpackSmapiFiles(
                    context,
                    packageInfo.applicationInfo.publicSourceDir,
                    true,
                    computePackageName(packageInfo),
                    versionCode
                )
            ) {
                showAlertDialog(root, R.string.error, R.string.error_failed_to_repair)
                return
            }
            val modAssetsManager = ModAssetsManager(root)
            modAssetsManager.checkModEnvironment { isConfirm ->
                if (isConfirm) {
                    val intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Crashes.trackError(e)
            showAlertDialog(root, R.string.error, e.localizedMessage)
        }
    }

    companion object {
        /**
         * 检查已安装MOD版本游戏
         * @param context 上下文
         * @return 软件包信息
         */
        fun getGamePackageInfo(context: Activity): PackageInfo? {
            val packageManager = context.packageManager
            return try {
                val packageInfo: PackageInfo? = try {
                    packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME, 0)
                } catch (ignored: PackageManager.NameNotFoundException) {
                    packageManager.getPackageInfo(Constants.TARGET_PACKAGE_NAME_SAMSUNG, 0)
                }
                packageInfo
            } catch (ignored: PackageManager.NameNotFoundException) {
                null
            }
        }
    }
}