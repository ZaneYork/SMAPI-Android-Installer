package com.zane.smapiinstaller;

import android.app.Application;

import com.lzy.okgo.OkGo;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkGo.getInstance().init(this);
    }
}
