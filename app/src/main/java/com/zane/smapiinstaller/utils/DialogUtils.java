package com.zane.smapiinstaller.utils;

import android.app.Activity;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.input.DialogInputExtKt;
import com.afollestad.materialdialogs.list.DialogListExtKt;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
import com.lmntrx.android.library.livin.missme.ProgressDialog;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.logic.CommonLogic;

import java.util.concurrent.atomic.AtomicReference;

import java9.util.function.BiConsumer;

/**
 * 对话框相关工具类
 *
 * @author Zane
 */
public class DialogUtils {
    private static Object currentDialog = null;

    public static Object getCurrentDialog() {
        return currentDialog;
    }

    public static void setCurrentDialog(Object currentDialog) {
        DialogUtils.currentDialog = currentDialog;
    }

    /**
     * 设置进度条状态
     *
     * @param view     context容器
     * @param dialog   对话框
     * @param message  消息
     * @param progress 进度
     */
    public static void setProgressDialogState(View view, ProgressDialog dialog, Integer message, Integer progress) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            if (progress != null) {
                dialog.setProgress(progress);
            }
            if (message != null) {
                dialog.setMessage(activity.getString(message));
            }
        });
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    public static void showAlertDialog(View view, int title, String message) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null).message(null, message, null).positiveButton(R.string.ok, null, null);
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    public static void showAlertDialog(View view, int title, int message) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null).message(message, null, null).positiveButton(R.string.ok, null, null);
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }

    /**
     * 显示确认对话框
     *
     * @param view     context容器
     * @param title    标题
     * @param message  消息
     * @param callback 回调
     */
    public static void showConfirmDialog(View view, int title, int message, BiConsumer<MaterialDialog, DialogAction> callback) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null).message(message, null, null).positiveButton(R.string.confirm, null, dialog -> {
                callback.accept(dialog, DialogAction.POSITIVE);
                return null;
            }).negativeButton(R.string.cancel, null, dialog -> {
                callback.accept(dialog, DialogAction.NEGATIVE);
                return null;
            });
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }
    public static void showConfirmDialog(Activity context, int title, int message, BiConsumer<MaterialDialog, DialogAction> callback) {
        CommonLogic.runOnUiThread(context, (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null).message(message, null, null).positiveButton(R.string.confirm, null, dialog -> {
                callback.accept(dialog, DialogAction.POSITIVE);
                return null;
            }).negativeButton(R.string.cancel, null, dialog -> {
                callback.accept(dialog, DialogAction.NEGATIVE);
                return null;
            });
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }

    /**
     * 显示确认对话框
     *
     * @param view     context容器
     * @param title    标题
     * @param message  消息
     * @param callback 回调
     */
    public static void showConfirmDialog(View view, int title, String message, BiConsumer<MaterialDialog, DialogAction> callback) {
        showConfirmDialog(view, title, message, R.string.confirm, R.string.cancel, callback);
    }

    /**
     * 显示确认对话框
     *
     * @param view         context容器
     * @param title        标题
     * @param message      消息
     * @param positiveText 确认文本
     * @param negativeText 取消文本
     * @param callback     回调
     */
    public static void showConfirmDialog(View view, int title, String message, int positiveText, int negativeText, BiConsumer<MaterialDialog, DialogAction> callback) {
        showConfirmDialog(view, title, message, positiveText, negativeText, false, callback);
    }

    public static void showConfirmDialog(View view, int title, String message, int positiveText, int negativeText, boolean isHtml, BiConsumer<MaterialDialog, DialogAction> callback) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR())
                    .title(title, null)
                    .positiveButton(positiveText, null, dialog -> {
                        callback.accept(dialog, DialogAction.POSITIVE);
                        return null;
                    }).negativeButton(negativeText, null, dialog -> {
                        callback.accept(dialog, DialogAction.NEGATIVE);
                        return null;
                    });
            if(isHtml){
                materialDialog.message(null, message, (dialogMessageSettings) -> {
                    dialogMessageSettings.html(null);
                    return null;
                });
            }
            else {
                materialDialog.message(null, message, null);
            }
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }

    /**
     * 显示进度条
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     * @return 对话框引用
     */
    public static AtomicReference<ProgressDialog> showProgressDialog(View view, int title, String message) {
        AtomicReference<ProgressDialog> reference = new AtomicReference<>();
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            ProgressDialog dialog = new ProgressDialog(activity);
            DialogUtils.setCurrentDialog(dialog);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
            reference.set(dialog);
        });
        return reference;
    }

    /**
     * 解散指定对话框
     *
     * @param view   view
     * @param dialog 对话框
     */
    public static void dismissDialog(View view, MaterialDialog dialog) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        Crashes.trackError(e);
                    }
                }
            });
        }
    }

    /**
     * 解散指定对话框
     *
     * @param view   view
     * @param dialog 对话框
     */
    public static void dismissDialog(View view, ProgressDialog dialog) {
        Activity activity = CommonLogic.getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if (dialog != null) {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        Crashes.trackError(e);
                    }
                }
            });
        }
    }

    /**
     * 解散当前对话框
     */
    public static void dismissDialog() {
        try {
            if (currentDialog != null) {
                if (currentDialog instanceof MaterialDialog) {
                    MaterialDialog dialog = (MaterialDialog) currentDialog;
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else if (currentDialog instanceof ProgressDialog) {
                    ProgressDialog dialog = (ProgressDialog) currentDialog;
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            Crashes.trackError(e);
        }
    }

    /**
     * 显示输入框
     *
     * @param view     context容器
     * @param title    标题
     * @param content  内容
     * @param hint     提示
     * @param prefill  预输入
     * @param callback 回调
     */
    public static void showInputDialog(View view, int title, int content, String hint, String prefill, BiConsumer<MaterialDialog, CharSequence> callback) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog dialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null).message(content, null, null);
            dialog = DialogInputExtKt.input(dialog, hint, null, prefill, null,
                    InputType.TYPE_CLASS_TEXT,
                    null, true, false, (materialDialog, text) -> {
                        callback.accept(materialDialog, text);
                        return null;
                    });
            DialogUtils.setCurrentDialog(dialog);
            dialog.show();
        });
    }

    /**
     * 显示列表单选框
     *
     * @param view     context容器
     * @param title    标题
     * @param items    列表
     * @param index    默认选择
     * @param callback 回调
     */
    public static void showSingleChoiceDialog(View view, int title, int items, int index, BiConsumer<MaterialDialog, Integer> callback) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null);
            materialDialog = DialogSingleChoiceExtKt.listItemsSingleChoice(materialDialog, items, null, null, index, false, (dialog, position, text) -> {
                callback.accept(dialog, position);
                return null;
            });
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }

    /**
     * 显示列表选择框
     *
     * @param view     context容器
     * @param title    标题
     * @param items    列表
     * @param callback 回调
     */
    public static void showListItemsDialog(View view, int title, int items, BiConsumer<MaterialDialog, Integer> callback) {
        CommonLogic.runOnUiThread(CommonLogic.getActivityFromView(view), (activity) -> {
            MaterialDialog materialDialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR()).title(title, null);
            materialDialog = DialogListExtKt.listItems(materialDialog, items, null, null, false, (dialog, position, text) -> {
                callback.accept(dialog, position);
                return null;
            });
            DialogUtils.setCurrentDialog(materialDialog);
            materialDialog.show();
        });
    }
}
