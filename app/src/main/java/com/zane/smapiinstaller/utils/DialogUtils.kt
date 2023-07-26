package com.zane.smapiinstaller.utils

import android.app.Activity
import android.text.InputType
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.lmntrx.android.library.livin.missme.ProgressDialog.Companion.STYLE_HORIZONTAL
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.logic.CommonLogic.getActivityFromView
import com.zane.smapiinstaller.logic.CommonLogic.runOnUiThread
import java.util.concurrent.atomic.AtomicReference

/**
 * 对话框相关工具类
 *
 * @author Zane
 */
object DialogUtils {
    var currentDialog: Any? = null

    /**
     * 设置进度条状态
     *
     * @param view     context容器
     * @param dialog   对话框
     * @param message  消息
     * @param progress 进度
     */
    @JvmStatic
    fun setProgressDialogState(view: View?, dialog: ProgressDialog, message: Int?, progress: Int?) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            if (progress != null) {
                dialog.setProgress(progress)
            }
            if (message != null) {
                dialog.setMessage(activity.getString(message))
            }
        }
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    @JvmStatic
    fun showAlertDialog(view: View?, title: Int, message: String?) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            val materialDialog =
                MaterialDialog(activity).title(title, null).message(null, message, null)
                    .positiveButton(R.string.ok, null, null)
            currentDialog = materialDialog
            materialDialog.show()
        }
    }

    @JvmStatic
    fun showAlertDialog(context: Activity?, title: Int, message: String?) {
        runOnUiThread(context) { activity ->
            val materialDialog =
                MaterialDialog(activity).title(title, null).message(null, message, null)
                    .positiveButton(R.string.ok, null, null)
            currentDialog = materialDialog
            materialDialog.show()
        }
    }

    /**
     * 显示警告对话框
     *
     * @param view    context容器
     * @param title   标题
     * @param message 消息
     */
    @JvmStatic
    fun showAlertDialog(view: View?, title: Int, message: Int) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            val materialDialog =
                MaterialDialog(activity).title(title, null).message(message, null, null)
                    .positiveButton(R.string.ok, null, null)
            currentDialog = materialDialog
            materialDialog.show()
        }
    }

    @JvmStatic
    fun showAlertDialog(context: Activity?, title: Int, message: Int) {
        runOnUiThread(context) { activity ->
            val materialDialog =
                MaterialDialog(activity).title(title, null).message(message, null, null)
                    .positiveButton(R.string.ok, null, null)
            currentDialog = materialDialog
            materialDialog.show()
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
    @JvmStatic
    fun showConfirmDialog(
        view: View?, title: Int, message: Int, callback: (MaterialDialog?, DialogAction) -> Unit
    ) {
        showConfirmDialog(getActivityFromView(view), title, message, callback)
    }

    @JvmStatic
    fun showConfirmDialog(
        context: Activity?,
        title: Int,
        message: Int,
        callback: (MaterialDialog?, DialogAction) -> Unit
    ) {
        runOnUiThread(context) { activity ->
            val materialDialog =
                MaterialDialog(activity).title(title, null).message(message, null, null)
                    .positiveButton(R.string.confirm, null) { dialog ->
                        callback.invoke(dialog, DialogAction.POSITIVE)
                    }.negativeButton(R.string.cancel, null) { dialog ->
                        callback.invoke(dialog, DialogAction.NEGATIVE)
                    }
            currentDialog = materialDialog
            materialDialog.show()
        }
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
    @JvmStatic
    fun showConfirmDialog(
        view: View?,
        title: Int,
        message: String?,
        positiveText: Int = R.string.confirm,
        negativeText: Int = R.string.cancel,
        isHtml: Boolean = false,
        callback: (MaterialDialog?, DialogAction) -> Unit = { _, _ -> }
    ) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            val materialDialog = MaterialDialog(activity).title(title, null)
                .positiveButton(positiveText, null) { dialog ->
                    callback.invoke(dialog, DialogAction.POSITIVE)
                }.negativeButton(negativeText, null) { dialog ->
                    callback.invoke(dialog, DialogAction.NEGATIVE)
                }
            if (isHtml) {
                materialDialog.message(
                    null, message
                ) {
                    this.html(null)
                }
            } else {
                materialDialog.message(null, message, null)
            }
            currentDialog = materialDialog
            materialDialog.show()
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
    @JvmStatic
    fun showProgressDialog(
        view: View?, title: Int, message: String
    ): AtomicReference<ProgressDialog> {
        val reference = AtomicReference<ProgressDialog>()
        runOnUiThread(getActivityFromView(view)) { activity ->
            val dialog = ProgressDialog(
                activity
            )
            currentDialog = dialog
            dialog.setMessage(message)
            dialog.setCancelable(false)
            dialog.setMax(100)
            dialog.setProgressStyle(STYLE_HORIZONTAL)
            dialog.show()
            reference.set(dialog)
        }
        return reference
    }

    @JvmStatic
    fun showProgressDialog(
        context: Activity?, title: Int, message: String
    ): AtomicReference<ProgressDialog> {
        val reference = AtomicReference<ProgressDialog>()
        runOnUiThread(context) { activity ->
            val dialog = ProgressDialog(
                activity
            )
            currentDialog = dialog
            dialog.setMessage(message)
            dialog.setCancelable(false)
            dialog.setMax(100)
            dialog.setProgressStyle(STYLE_HORIZONTAL)
            dialog.show()
            reference.set(dialog)
        }
        return reference
    }

    /**
     * 解散指定对话框
     *
     * @param view   view
     * @param dialog 对话框
     */
    @JvmStatic
    fun dismissDialog(view: View?, dialog: MaterialDialog?) {
        val activity = getActivityFromView(view)
        if (activity != null && !activity.isFinishing) {
            activity.runOnUiThread(Runnable {
                if (dialog != null && dialog.isShowing) {
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Crashes.trackError(e)
                    }
                }
            })
        }
    }

    /**
     * 解散指定对话框
     *
     * @param view   view
     * @param dialog 对话框
     */
    @JvmStatic
    fun dismissDialog(view: View?, dialog: ProgressDialog?) {
        val activity = getActivityFromView(view)
        if (activity != null && !activity.isFinishing) {
            activity.runOnUiThread(Runnable {
                if (dialog != null) {
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Crashes.trackError(e)
                    }
                }
            })
        }
    }

    @JvmStatic
    fun dismissDialog(activity: Activity?, dialog: ProgressDialog?) {
        if (activity != null && !activity.isFinishing) {
            activity.runOnUiThread(Runnable {
                if (dialog != null) {
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Crashes.trackError(e)
                    }
                }
            })
        }
    }

    /**
     * 解散当前对话框
     */
    @JvmStatic
    fun dismissDialog() {
        try {
            currentDialog?.let {
                if (it is MaterialDialog) {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                } else if (it is ProgressDialog) {
                    it.dismiss()
                }
            }
        } catch (e: Exception) {
            Crashes.trackError(e)
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
    @JvmStatic
    fun showInputDialog(
        view: View?,
        title: Int,
        content: Int,
        hint: String?,
        prefill: String?,
        allowEmpty: Boolean = false,
        callback: (MaterialDialog?, CharSequence) -> Unit = { _: MaterialDialog?, _: CharSequence -> }
    ) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            var dialog = MaterialDialog(activity).title(title, null).message(content, null, null)
            dialog = dialog.input(
                hint, null, prefill, null, InputType.TYPE_CLASS_TEXT, null, true, allowEmpty
            ) { materialDialog: MaterialDialog?, text: CharSequence? ->
                text?.let { callback.invoke(materialDialog, it) }
            }
            currentDialog = dialog
            dialog.show()
        }
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
    @JvmStatic
    fun showSingleChoiceDialog(
        view: View?, title: Int, items: Int, index: Int, callback: (MaterialDialog?, Int) -> Unit
    ) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            var materialDialog = MaterialDialog(activity).title(title, null)
            materialDialog = materialDialog.listItemsSingleChoice(
                items, null, null, index, false, -1, -1
            ) { dialog: MaterialDialog?, position: Int?, _: CharSequence? ->
                position?.let { callback.invoke(dialog, it) }
            }
            currentDialog = materialDialog
            materialDialog.show()
        }
    }

    /**
     * 显示列表选择框
     *
     * @param view     context容器
     * @param title    标题
     * @param items    列表
     * @param callback 回调
     */
    @JvmStatic
    fun showListItemsDialog(
        view: View?, title: Int, items: Int, callback: (MaterialDialog?, Int) -> Unit
    ) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            var materialDialog = MaterialDialog(activity).title(title, null)
            materialDialog = materialDialog.listItems(
                items, null, null, false
            ) { dialog: MaterialDialog?, position: Int?, _: CharSequence? ->
                position?.let { callback.invoke(dialog, it) }
            }
            currentDialog = materialDialog
            materialDialog.show()
        }
    }

    @JvmStatic
    fun showListItemsDialog(
        view: View?, title: Int, items: List<String>, callback: (MaterialDialog?, Int) -> Unit
    ) {
        runOnUiThread(getActivityFromView(view)) { activity ->
            var materialDialog = MaterialDialog(activity).title(title, null)
            materialDialog = materialDialog.listItems(
                null, items, null, false
            ) { dialog, position, _ ->
                callback.invoke(dialog, position)
            }
            currentDialog = materialDialog
            materialDialog.show()
        }
    }
}