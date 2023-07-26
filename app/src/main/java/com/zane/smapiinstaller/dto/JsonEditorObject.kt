package com.zane.smapiinstaller.dto

import android.webkit.JavascriptInterface

/**
 * @author Zane
 */
class JsonEditorObject
    (
    private var text: String,
    @get:JavascriptInterface val mode: String,
    @get:JavascriptInterface val language: String,
    @get:JavascriptInterface val isEditable: Boolean,
    @get:JavascriptInterface val height: Int,
    private val setterCallback: ((String) -> Unit)? = null
) {

    @JavascriptInterface
    fun getText(): String {
        return text
    }

    @JavascriptInterface
    fun setText(text: String) {
        this.text = text
        setterCallback?.invoke(text)
    }
}