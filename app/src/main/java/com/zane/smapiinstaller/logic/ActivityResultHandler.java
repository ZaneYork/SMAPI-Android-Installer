package com.zane.smapiinstaller.logic;

import android.content.Intent;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiConsumer;

/**
 * @author Zane
 */
public class ActivityResultHandler {
    public static final int REQUEST_CODE_APP_INSTALL = 1001;
    public static final int REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION = 1002;
    public static final int REQUEST_CODE_OBB_FILES_ACCESS_PERMISSION = 1003;

    public static ConcurrentHashMap<Integer, BiConsumer<Integer, Intent>> listenerMap = new ConcurrentHashMap<>();

    public static void registerListener(int requestCode, BiConsumer<Integer, Intent> listener) {
        listenerMap.put(requestCode, listener);
    }

    public static void triggerListener(int requestCode, int resultCode, Intent data) {
        BiConsumer<Integer, Intent> biConsumer = listenerMap.get(requestCode);
        if(biConsumer != null) {
            biConsumer.accept(resultCode, data);
        }
    }
}
