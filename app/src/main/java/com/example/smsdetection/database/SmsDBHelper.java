package com.example.smsdetection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smsdetection.entity.SmsInfo;

import java.util.ArrayList;
import java.util.List;


public class SmsDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sms.db";
    // 账单信息表
    private static final String TABLE_SMS_INFO = "sms_info";
    private static final int DB_VERSION = 1;
    private static SmsDBHelper mHelper = null;
    private SQLiteDatabase mRDB = null;
    private SQLiteDatabase mWDB = null;

    private SmsDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 利用单例模式获取数据库帮助器的唯一实例
    public static SmsDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new SmsDBHelper(context);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = mHelper.getWritableDatabase();
        }
        return mWDB;
    }

    // 关闭数据库连接
    public void closeLink() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }

        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    // 创建数据库，执行建表语句
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建账单信息表
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_SMS_INFO +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " datetime VARCHAR NOT NULL," +
                " sender VARCHAR NOT NULL," +
                " content VARCHAR NOT NULL," +
                " type INTEGER NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 保存一条订单记录
    public long save(SmsInfo sms) {
        ContentValues cv = new ContentValues();
        cv.put("datetime", sms.datetime);
        cv.put("sender", sms.sender);
        cv.put("content", sms.content);
        cv.put("type", sms.type);
        return mWDB.insert(TABLE_SMS_INFO, null, cv);
    }

    // 查询所有的信息列表
    public List<SmsInfo> queryAllSmsInfo() {
        List<SmsInfo> list = new ArrayList<>();
        Cursor cursor = mRDB.query(TABLE_SMS_INFO, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            SmsInfo info = new SmsInfo();
            info.id = cursor.getInt(0);
            info.datetime = cursor.getString(1);
            info.sender = cursor.getString(2);
            info.content = cursor.getString(3);
            info.type = cursor.getInt(4);
            list.add(info);
        }
        return list;
    }

    public SmsInfo querySmsInfoById(int sms_id){
        SmsInfo info = null;
        Cursor cursor = mRDB.query(TABLE_SMS_INFO, null, "_id=?", new String[]{String.valueOf(sms_id)}, null, null, null);
        if(cursor.moveToNext()){
            info = new SmsInfo();
            info.id = cursor.getInt(0);
            info.datetime = cursor.getString(1);
            info.sender = cursor.getString(2);
            info.content = cursor.getString(3);
            info.type = cursor.getInt(4);
        }
        return info;
    }

    // 根据ID删除信息
    public void deleteSmsInfoById(int id) {
        mWDB.delete(TABLE_SMS_INFO, "_id=?", new String[]{String.valueOf(id)});
    }

    // 删除所有信息
    public void deleteAllSmsInfo() {
        mWDB.delete(TABLE_SMS_INFO, "1=1", null);
    }

}
