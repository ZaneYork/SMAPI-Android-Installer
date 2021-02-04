package com.zane.smapiinstaller.utils.function;

import android.text.Editable;

/**
 * @author Zane
 */
public abstract class TextChangedWatcher implements android.text.TextWatcher {
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void onTextChanged(CharSequence s, int start, int before, int count);

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTextChanged(Editable s) {}
}
