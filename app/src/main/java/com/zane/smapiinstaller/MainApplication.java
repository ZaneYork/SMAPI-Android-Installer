package com.zane.smapiinstaller;

import android.app.Application;
import android.content.Context;

import com.hjq.language.LanguagesManager;
import com.lzy.okgo.OkGo;
import com.zane.smapiinstaller.entity.DaoMaster;
import com.zane.smapiinstaller.entity.DaoSession;
import com.zane.smapiinstaller.utils.DbOpenHelper;
import com.zane.smapiinstaller.utils.GzipRequestInterceptor;

import org.greenrobot.greendao.database.Database;

import lombok.Getter;
import okhttp3.OkHttpClient;

@Getter
public class MainApplication extends Application {
    private DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new GzipRequestInterceptor())//开启Gzip压缩
                .build();
        OkGo.getInstance().setOkHttpClient(okHttpClient).init(this);
        LanguagesManager.init(this);
        // note: DevOpenHelper is for dev only, use a OpenHelper subclass instead
        DbOpenHelper helper = new DbOpenHelper(this, "installer-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    @Override
    protected void attachBaseContext(Context base) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(LanguagesManager.attach(base));
    }
}
