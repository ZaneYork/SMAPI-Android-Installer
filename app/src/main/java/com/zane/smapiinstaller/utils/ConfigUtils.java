package com.zane.smapiinstaller.utils;

import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.entity.AppConfig;
import com.zane.smapiinstaller.entity.AppConfigDao;
import com.zane.smapiinstaller.entity.DaoSession;

/**
 * @author Zane
 */
public class ConfigUtils {
    public static <T> AppConfig getConfig(MainApplication application, String key, T defaultValue) {
        DaoSession daoSession = application.getDaoSession();
        AppConfigDao appConfigDao = daoSession.getAppConfigDao();
        AppConfig appConfig = appConfigDao.queryBuilder().where(AppConfigDao.Properties.Name.eq(key)).build().unique();
        if(appConfig == null) {
            if(defaultValue != null){
                appConfig = new AppConfig(null, key, String.valueOf(defaultValue));
            }
            else {
                appConfig = new AppConfig(null, key, null);
            }
        }
        return appConfig;
    }
    public static void saveConfig(MainApplication application, AppConfig config){
        DaoSession daoSession = application.getDaoSession();
        AppConfigDao appConfigDao = daoSession.getAppConfigDao();
        appConfigDao.insertOrReplace(config);
    }
}
