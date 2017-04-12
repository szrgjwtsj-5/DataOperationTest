package com.whx.dataoperationtest.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by whx on 2017/4/11.
 */

public class FileHelper {
    private static final String TAG = "FileHelper";

    private DBOpenHelper dbOpenHelper;

    public FileHelper(Context context) {
        dbOpenHelper = new DBOpenHelper(context);
    }

    /**
     * 获得指定URI的每条线程已经下载的文件长度
     * @param path 下载链接
     * @return
     */
    public Map<Integer, Integer> getData(String path) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select thread_id, down_length from file_down_log where down_path=?",
                new String[]{path});

        Map<Integer, Integer> data = new HashMap<>();

        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            //把线程id与该线程已下载的长度存放到data哈希表中
            data.put(cursor.getInt(0), cursor.getInt(1));
        }
        cursor.close();
        db.close();

        return data;
    }

    /**
     * 保存每条线程已经下载的文件长度
     * @param path 下载的路径
     * @param data 现在的线程ID和已经下载的长度的集合
     */
    public void save(String path, Map<Integer, Integer> data) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        //开启事务,因为此处需要插入多条数据
        db.beginTransaction();

        try {
            for(Map.Entry<Integer, Integer> entry : data.entrySet()) {
                db.execSQL("insert into file_down_log(down_path, thread_id, down_length) values(?,?,?)",
                        new Object[]{path, entry.getKey(), entry.getValue()});
            }
            //设置一个事务成功的标志,如果成功就提交事务,如果没调用该方法的话那么事务回滚
            //就是上面的数据库操作撤销
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.i(TAG, "数据库保存异常");
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    /**
     * 实时更新每条线程已经下载的文件长度
     * @param path
     * @param threadId
     * @param pos
     */
    public void update(String path,int threadId,int pos)
    {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        //更新特定下载路径下特定线程已下载的文件长度
        db.execSQL("update file_down_log set down_length=? where down_path=? and thread_id=?",
                new Object[]{pos, path, threadId});
        db.close();
    }
    /**
     *当文件下载完成后，删除对应的下载记录
     *@param path
     */
    public void delete(String path)
    {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.execSQL("delete from file_down_log where down_path=?", new Object[]{path});
        db.close();
    }
}
