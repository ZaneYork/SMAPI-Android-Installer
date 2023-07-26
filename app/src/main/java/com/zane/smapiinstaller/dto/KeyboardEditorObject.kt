package com.zane.smapiinstaller.dto

import android.webkit.JavascriptInterface

/**
 * @author Zane
 */
class KeyboardEditorObject (
    private var text: String,
    @get:JavascriptInterface val language: String,
    @get:JavascriptInterface val height: Int,
    @get:JavascriptInterface val width: Int,
    @get:JavascriptInterface val scale: Float,
    @get:JavascriptInterface val isLandscape: Boolean,
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