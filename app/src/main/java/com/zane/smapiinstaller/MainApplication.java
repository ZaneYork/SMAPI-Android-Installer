package com.zane.smapiinstaller;

import android.app.Application;
import android.content.Context;

import com.hjq.language.LanguagesManager;
import com.lzy.okgo.OkGo;
import com.zane.smapiinstaller.utils.GzipRequestInterceptor;

import okhttp3.OkHttpClient;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new GzipRequestInterceptor())//开启Gzip压缩
                .build();
        OkGo.getInstance().setOkHttpClient(okHttpClient).init(this);
        LanguagesManager.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(LanguagesManager.attach(base));
    }
}
