package com.zane.smapiinstaller

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.hjq.language.MultiLanguages
import com.lzy.okgo.OkGo
import com.zane.smapiinstaller.entity.DaoMaster
import com.zane.smapiinstaller.entity.DaoSession
import com.zane.smapiinstaller.utils.DbOpenHelper
import okhttp3.OkHttpClient

/**
 * @author Zane
 */
class MainApplication : Application() {
    //<editor-fold defaultstate="collapsed" desc="delombok">
    //</editor-fold>
    lateinit var daoSession: DaoSession
        private set

    override fun onCreate() {
        super.onCreate()
        val okHttpClient =  //开启Gzip压缩
            //                .addInterceptor(new GzipRequestInterceptor())
            OkHttpClient.Builder().build()
        OkGo.getInstance().setOkHttpClient(okHttpClient).init(this)
        MultiLanguages.init(this)
        // note: DevOpenHelper is for dev only, use a OpenHelper subclass instead
        val helper = DbOpenHelper(this, "installer-db")
        val db = helper.writableDb
        daoSession = DaoMaster(db).newSession()
    }

    override fun attachBaseContext(base: Context) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(MultiLanguages.attach(base))
        MultiDex.install(this)
    }
}