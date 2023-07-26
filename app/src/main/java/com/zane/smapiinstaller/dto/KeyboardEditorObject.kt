package com.zane.smapiinstaller.dto;

import android.webkit.JavascriptInterface;
import java.util.function.Consumer;

/**
 * @author Zane
 */
public class KeyboardEditorObject {
    private String text;
    private String language;
    private int height;
    private int width;
    private float scale;
    private boolean landscape;
    private Consumer<String> setterCallback;

    @JavascriptInterface
    public String getText() {
        return text;
    }

    @JavascriptInterface
    public void setText(String text) {
        this.text = text;
        if (setterCallback != null) {
            setterCallback.accept(text);
        }
    }

    @JavascriptInterface
    public int getHeight() {
        return height;
    }

    @JavascriptInterface
    public int getWidth() {
        return width;
    }

    @JavascriptInterface
    public float getScale() {
        return scale;
    }

    @JavascriptInterface
    public boolean isLandscape() {
        return landscape;
    }

    @JavascriptInterface
    public String getLanguage() {
        return language;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public KeyboardEditorObject(final String text, final String language, final int height, final int width, final float scale, final boolean landscape, final Consumer<String> setterCallback) {
        this.text = text;
        this.language = language;
        this.height = height;
        this.width = width;
        this.scale = scale;
        this.landscape = landscape;
        this.setterCallback = setterCallback;
    }
    //</editor-fold>
}
