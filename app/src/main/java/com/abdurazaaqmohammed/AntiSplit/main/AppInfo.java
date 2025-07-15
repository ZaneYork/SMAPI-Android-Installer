package com.abdurazaaqmohammed.AntiSplit.main;

import android.graphics.drawable.Drawable;

public class AppInfo {
    String name;
    String packageName;
    Drawable icon;
    long lastUpdated;
    long firstInstall;

    public AppInfo(String name, Drawable icon, String packageName, long lastUpdated, long firstInstall) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.lastUpdated = lastUpdated;
        this.firstInstall = firstInstall;
    }
}
