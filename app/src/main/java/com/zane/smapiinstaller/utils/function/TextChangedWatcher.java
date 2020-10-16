package com.zane.smapiinstaller.utils.function;

import android.text.Editable;

/**
 * @author Zane
 */
public abstract class TextChangedWatcher implements android.text.TextWatcher {
    /**
     * Do nothing
     * @param s origin string
     * @param start modify position
     * @param count modify count
     * @param after modified string
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    /**
     * Text changed event
     * @param s modified string
     * @param start modify position
     * @param count modify count
     */
    @Override
    public abstract void onTextChanged(CharSequence s, int start, int before, int count);

    /**
     * Do nothing
     * @param s target view
     */
    @Override
    public void afterTextChanged(Editable s) {}
}
