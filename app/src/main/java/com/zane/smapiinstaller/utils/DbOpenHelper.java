package com.zane.smapiinstaller.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zane.smapiinstaller.entity.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * @author Zane
 */
public class DbOpenHelper extends DaoMaster.OpenHelper {
    public DbOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        if (newVersion == 1) {
//            TranslationResultDao.dropTable(db, true);
//        }
        onCreate(db);
    }
}
