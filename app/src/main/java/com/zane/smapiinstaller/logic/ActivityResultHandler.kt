package com.zane.smapiinstaller.logic

import android.content.Intent
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Zane
 */
object ActivityResultHandler {
    const val REQUEST_CODE_APP_INSTALL = 1001
    const val REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION = 1002
    const val REQUEST_CODE_OBB_FILES_ACCESS_PERMISSION = 1003
    const val REQUEST_CODE_DATA_FILES_ACCESS_PERMISSION = 1004
    var listenerMap = ConcurrentHashMap<Int, (requestCode: Int, data: Intent?) -> Unit>()

    @JvmStatic
    fun registerListener(requestCode: Int, listener: (requestCode: Int, data: Intent?) -> Unit) {
        listenerMap[requestCode] = listener
    }

    fun triggerListener(requestCode: Int, resultCode: Int, data: Intent?) {
        val biConsumer = listenerMap[requestCode]
        biConsumer?.invoke(resultCode, data)
    }
}