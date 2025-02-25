package com.example.smsdetection.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.smsdetection.database.SmsDBHelper;
import com.example.smsdetection.entity.SmsInfo;
import com.example.smsdetection.utils.ChatClient;
import com.example.smsdetection.utils.ToastUtil;
import com.example.smsdetection.utils.Utils;

import java.util.Calendar;

public class SmsGetObserver extends ContentObserver {

    private final Context mContext;
    private SmsDBHelper mDBHelper;

    public SmsGetObserver(Context context) {
        super(new Handler(Looper.getMainLooper()));
        this.mContext = context;
        this.mDBHelper = SmsDBHelper.getInstance(context);
    }

    @SuppressLint("Range")
    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);

//            if(!status){
//                return;
//            }
        // onChange会多次调用，收到一条短信会调用两次onChange
        // mUri===content://sms/raw/20
        // mUri===content://sms/inbox/20
        // 安卓7.0以上系统，点击标记为已读，也会调用一次
        // mUri===content://sms
        // 收到一条短信都是uri后面都会有确定的一个数字，对应数据库的_id，比如上面的20
        if (uri == null) {
            return;
        }
        if (uri.toString().contains("content://sms/raw") ||
                uri.toString().equals("content://sms")) {
            return;
        }

        // 通过内容解析器获取符合条件的结果集游标
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"address", "body", "date"}, null, null, "date DESC");
        if (cursor.moveToNext()) {
            // 短信的发送号码
            String sender = cursor.getString(cursor.getColumnIndex("address"));
            // 短信内容
            String content = cursor.getString(cursor.getColumnIndex("body"));
            Log.d("DEBUG", String.format("sender:%s,content:%s", sender, content));
            // 添加入数据库
            SmsInfo info = new SmsInfo();
            info.datetime = Utils.getDate(Calendar.getInstance()) + "=" + Utils.getNowTime();
            info.sender = sender;
            info.content = content;
            String result = null;
//            try {
////                result = ChatClient.callWithMessage(info.content, ChatClient.QWEN1_5b);
//            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
//                result = e.getMessage();
//            }
            Log.d("RESULT", "onChange: "+result);

            assert result != null;
            if (result.startsWith("否")) {
                info.type = SmsInfo.SMS_TYPE_COMMON;
                ToastUtil.show(mContext, "这是普通信息！");
            }else if (result.startsWith("是")){
                info.type = SmsInfo.SMS_TYPE_DECEIVE;
                ToastUtil.show(mContext, "这可能是诈骗信息，请注意防范！");
            }else{
                ToastUtil.show(mContext, "出错啦！");
                return;
            }
            if (mDBHelper.save(info) > 0) {
                ToastUtil.show(mContext, "短信已存入EagleSight历史记录！");
            }
            cursor.close();
        }
    }
}
