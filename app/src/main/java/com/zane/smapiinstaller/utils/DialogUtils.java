package com.zane.smapiinstaller.utils;

import android.app.Activity;
import android.app.Dialog;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.logic.CommonLogic;

import java.util.concurrent.atomic.AtomicReference;

import androidx.drawerlayout.widget.DrawerLayout;

/**
 * 对话框相关工具类
 */
public class DialogUtils {
    /**
     * 设置进度条状态
     *
     * @param view     context容器
     * @param dialog   对话框
     * @param message  消息
     * @param progress 进度
     */
    public static void setProgressDialogState(View view, MaterialDialog dialog, int message, int progress) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing() && !dialog.isCancelled()) {
            activity.runOnUiThread(() -> {
                dialog.setProgress(progress);
                dialog.setContent(message);
            });
        }
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    public static void showAlertDialog(View view, int title, String message) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    public static void showAlertDialog(View view, int title, int message) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }

    /**
     * 显示确认对话框
     *
     * @param view     context容器
     * @param title    标题
     * @param message  消息
     * @param callback 回调
     */
    public static void showConfirmDialog(View view, int title, int message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    /**
     * 显示确认对话框
     *
     * @param view     context容器
     * @param title    标题
     * @param message  消息
     * @param callback 回调
     */
    public static void showConfirmDialog(View view, int title, String message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    /**
     * 显示进度条
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     * @return 对话框引用
     */
    public static AtomicReference<MaterialDialog> showProgressDialog(View view, int title, String message) {
        Activity activity = CommonLogic.getActivityFromView(view);
        AtomicReference<MaterialDialog> reference = new AtomicReference<>();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                MaterialDialog dialog = new MaterialDialog.Builder(activity)
                        .title(title)
                        .content(message)
                        .progress(false, 100, true)
                        .cancelable(false)
                        .show();
                reference.set(dialog);
            });
        }
        return reference;
    }

    public static void dismissDialog(View view, MaterialDialog dialog) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            if (dialog != null && !dialog.isCancelled()) {
                dialog.dismiss();
            }
        }
    }

    public static void showInputDialog(Activity activity, int title, int content, String hint, String prefill, MaterialDialog.InputCallback callback) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity)
                    .title(title)
                    .content(content)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(hint, prefill, callback)
                    .show()
            );
        }
    }
}
