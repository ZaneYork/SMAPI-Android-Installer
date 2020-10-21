package com.zane.smapiinstaller.dto;

import android.webkit.JavascriptInterface;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.JsonUtil;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;

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
    private int width;
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
    public int getWidth() {
        return width;
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
