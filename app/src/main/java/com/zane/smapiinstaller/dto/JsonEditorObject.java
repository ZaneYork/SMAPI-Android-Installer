package com.zane.smapiinstaller.dto;

import android.webkit.JavascriptInterface;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

/**
 * @author Zane
 */
@AllArgsConstructor
public class JsonEditorObject {
    private String text;
    private String mode;
    private String language;
    private boolean editable;
    private int height;
    private Consumer<String> setterCallback;

    @JavascriptInterface
    public String getText() {
        return text;
    }

    @JavascriptInterface
    public void setText(String text) {
        this.text = text;
        if(setterCallback != null) {
            setterCallback.accept(text);
        }
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
