package com.reandroid.apkeditor.merge;

import com.reandroid.apkeditor.merge.Merger;

public class LogUtil {
    private static Merger.LogListener logListener;

    public static boolean logEnabled;

    public static void setLogListener(Merger.LogListener listener) {
        logListener = listener;
    }

    public static void logMessage(CharSequence msg) {
        if (logListener != null && logEnabled) {
            logListener.onLog(msg);
        }
    }
    public static void logMessage(int resID) {
        if (logListener != null && logEnabled) {
            logListener.onLog(resID);
        }
    }
}