package com.whx.dataoperationtest.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库管理类
 * Created by whx on 2017/4/11.
 */

public class DBOpenHelper extends SQLiteOpenHelper{

    public DBOpenHelper(Context context) {
        super(context, "downs.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //数据库的结构为:表名:file_down_log 字段:id, down_path:当前下载的资源,
        //thread_id:下载的线程id，down_length:线程下载的最后位置
        db.execSQL("CREATE TABLE IF NOT EXISTS file_down_log " +
                "(id integer primary key autoincrement," +
                " down_path varchar(100)," +
                " thread_id INTEGER, down_length INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //当版本号发生改变时调用该方法,这里删除数据表,在实际业务中一般是要进行数据备份的
        db.execSQL("DROP TABLE IF EXISTS file_down_log");
        onCreate(db);
    }
}
