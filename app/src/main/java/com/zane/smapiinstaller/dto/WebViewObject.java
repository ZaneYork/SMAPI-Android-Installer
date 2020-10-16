package com.zane.smapiinstaller.dto;

import android.webkit.JavascriptInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zane
 */
@AllArgsConstructor
public class WebViewObject {
    private String text;
    private String mode;
    private String language;
    private boolean editable;
    private int height;

    @JavascriptInterface
    public String getText() {
        return text;
    }

    @JavascriptInterface
    public void setText(String text) {
        this.text = text;
    }

    @JavascriptInterface
    public boolean isEditable() {
        return editable;
    }

    @JavascriptInterface
    public int getHeight() {
        return height;
    }

    @JavascriptInterface
    public String getMode() {
        return mode;
    }

    @JavascriptInterface
    public String getLanguage() {
        return language;
    }
}
