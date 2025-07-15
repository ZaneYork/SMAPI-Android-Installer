package com.abdurazaaqmohammed.AntiSplit.main;

import android.os.Build;

public class LegacyUtils {

    public static final boolean supportsFileChannel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    public static final boolean supportsWriteExternalStorage = Build.VERSION.SDK_INT < 30;
    public static final boolean aboveSdk20 = Build.VERSION.SDK_INT > 20;
}