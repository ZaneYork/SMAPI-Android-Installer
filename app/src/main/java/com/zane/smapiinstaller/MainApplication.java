package com.zane.smapiinstaller;

import android.app.Application;
import android.content.Context;

import com.hjq.language.LanguagesManager;
import com.lzy.okgo.OkGo;
import com.zane.smapiinstaller.entity.DaoMaster;
import com.zane.smapiinstaller.entity.DaoSession;
import com.zane.smapiinstaller.utils.DbOpenHelper;

import org.greenrobot.greendao.database.Database;

import androidx.multidex.MultiDex;
import lombok.Getter;
import okhttp3.OkHttpClient;

/**
 * @author Zane
 */
@Getter
public class MainApplication extends Application {
    private DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //开启Gzip压缩
//                .addInterceptor(new GzipRequestInterceptor())
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
        MultiDex.install(this);
    }
}
