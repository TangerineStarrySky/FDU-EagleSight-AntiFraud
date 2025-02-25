package com.example.smsdetection;


import static com.example.smsdetection.model.NavViewKt.NavView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.material3.MaterialTheme;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.platform.ComposeView;
//import androidx.compose.ui.platform.setContent;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.smsdetection.database.SmsDBHelper;
import com.example.smsdetection.entity.SmsInfo;
import com.example.smsdetection.model.AppViewModel;
import com.example.smsdetection.model.ChatCallback;
import com.example.smsdetection.service.SmsDetectService;
import com.example.smsdetection.service.SmsMonitorService;
import com.example.smsdetection.utils.AccessibilityServiceUtil;
import com.example.smsdetection.utils.ChatClient;
import com.example.smsdetection.utils.PermissionUtil;
import com.example.smsdetection.utils.ToastUtil;
import com.example.smsdetection.utils.Utils;

import java.util.Calendar;
import android.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainActivity extends ComponentActivity implements View.OnClickListener {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private boolean status = false;
    private SmsDBHelper mDBHelper;
    private Button status_btn;
    private EditText input_sms;
    private TextView output_result;

    private SmsGetObserver mObserver;

    private static final String[] PERMISSIONS = new String[]{
//            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    private static final int REQUEST_CODE_SMS = 1;
    ChatClient chatClient;
    // 11.30
    private AppViewModel appViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取Compose容器
//        FrameLayout composeContainer = findViewById(R.id.compose_container);

        // 创建ComposeView
//        ComposeView composeView = new ComposeView(this);
//        composeContainer.addView(composeView);

        // 设置Compose内容
//        composeView.setContent {
//            MaterialTheme {
//                NavView()
//            }
//        }

//        chatClient = new ChatClient(this.getApplication());

        appViewModel = new AppViewModel(this.getApplication());
        Log.d("DEBUG", "onCreate: new AppViewModel");
        appViewModel.getModelList().get(0).startChat();

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("鹰眼智能识别");
        TextView tv_history = findViewById(R.id.tv_history);
        tv_history.setText("历史记录");

        findViewById(R.id.tv_history).setOnClickListener(this);
        findViewById(R.id.btn_detect).setOnClickListener(this);
        findViewById(R.id.tv_learning).setOnClickListener(this);
        findViewById(R.id.tv_feedback).setOnClickListener(this);

        status_btn = findViewById(R.id.status_switch);
        status_btn.setOnClickListener(this);

        input_sms = findViewById(R.id.input_sms);
        output_result = findViewById(R.id.output_result);

        mDBHelper = SmsDBHelper.getInstance(this);
        mDBHelper.openReadLink();
        mDBHelper.openWriteLink();

        Uri uri = Uri.parse("content://sms");
        mObserver = new SmsGetObserver(this);
        getContentResolver().registerContentObserver(uri, true, mObserver);

        // 设置前台应用
//        Intent serviceIntent = new Intent(this, SmsMonitorService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent); // Android 8.0及以上版本
//        }
    }


    private class SmsGetObserver extends ContentObserver {

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

            if(!status){
                return;
            }
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
//                String result = null;
//                try {
//                    result = chatClient.callWithMessage(info.content, ChatClient.QWEN1_5b);
//                } catch (ApiException | NoApiKeyException | InputRequiredException e) {
//                    result = e.getMessage();
//                }
                appViewModel.getChatState().myChat(info.content, false, new ChatCallback() {
                    @Override
                    public void onResult(@NonNull String result) {
                        Log.d("RESULT", "onChange: " + result);

                        if (result.startsWith("否")) {
                            info.type = SmsInfo.SMS_TYPE_COMMON;
                            ToastUtil.show(mContext, "这是普通信息！");
                        }else if (result.startsWith("是")){
                            info.type = SmsInfo.SMS_TYPE_DECEIVE;
//                    ToastUtil.show(mContext, "这可能是诈骗信息，请注意防范！");
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("这可能是诈骗信息，请注意防范！");
                            builder.setPositiveButton("查看详情", (dialog, which) -> {
//                                String detail = null;
//                                try {
//                                    detail = chatClient.callForDetail(content, ChatClient.QWEN1_5b);
//                                } catch (ApiException | NoApiKeyException | InputRequiredException e) {
//                                    ToastUtil.show(mContext, e.getMessage());
//                                }
                                appViewModel.getChatState().myChat(info.content, true, new ChatCallback() {
                                    @Override
                                    public void onResult(@NonNull String detail) {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                        builder2.setMessage(detail);
                                        builder2.setPositiveButton("好的",null);
                                        builder2.create().show();
                                    }
                                });
                            });
                            builder.setNegativeButton("知道了", null);
                            builder.create().show();
                        }else{
                            ToastUtil.show(mContext, "出错啦！");
                            return;
                        }
                        if (mDBHelper.save(info) > 0) {
                            ToastUtil.show(mContext, "短信已存入EagleSight历史记录！");
                        }
                        cursor.close();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.closeLink();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if(vid == R.id.status_switch){
            if(status_btn.getText().toString().equals(getString(R.string.open_analysis))
                    && PermissionUtil.checkPermission(this, PERMISSIONS, REQUEST_CODE_SMS)){
                status = true;
                status_btn.setText(R.string.close_analysis);
                ToastUtil.show(this, "鹰眼智能识别已开启！");
            } else if (status_btn.getText().toString().equals(getString(R.string.close_analysis))){
                status = false;
                status_btn.setText(R.string.open_analysis);
                ToastUtil.show(this, "鹰眼智能识别已关闭！");
            }
        }else if (vid == R.id.tv_history){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, HistoryActivity.class);
//            intent.putExtra("chat_state", appViewModel.getChatState());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (vid == R.id.tv_learning){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LearningActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (vid == R.id.tv_feedback){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, FeedbackActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(vid == R.id.btn_detect){
            Utils.hideOneInputMethod(MainActivity.this, view);
            String message = String.valueOf(input_sms.getText());
            if(message.isBlank()) {
                ToastUtil.show(this, "请输入短信内容！");
                return;
            }
            // 不开启智能识别的情况下也可以支持手动输入检测。
            // 智能识别只是允许实时检测收到的短信
//            if(!status){
//                output_result.setText("");
//                ToastUtil.show(this, "鹰眼智能识别未开启！");
//                return;
//            }
//            output_result.setText(String.valueOf(SmsDetectService.isStart()));

//            check(message, ChatClient.QWEN1_5b);
            checkOnDevice(message, this);
        }
    }

    private void checkOnDevice(String message, Context context){
        try {
            appViewModel.getChatState().myChat(message, false, new ChatCallback() {
                @Override
                public void onResult(@NonNull String result) {
                    Log.d("DEBUG", "callWithMessageOnDevice onResult: "+result);
                    if(result.startsWith("否")) {
                        output_result.setText("该短信为普通短信。");
                        SmsInfo info = new SmsInfo();
                        info.sender = "手动输入";
                        info.type = SmsInfo.SMS_TYPE_COMMON;
                        info.content = message;
                        info.datetime = Utils.getDate(Calendar.getInstance())+"="+Utils.getNowTime();
                        if (mDBHelper.save(info)>0){
                            ToastUtil.show(context, "短信已存入历史记录！");
                        }
                    }
                    else if(result.startsWith("是")) {
                        appViewModel.getChatState().myChat(message, true, new ChatCallback() {
                            @Override
                            public void onResult(@NonNull String detail) {
                                output_result.setText("该短信可能为诈骗短信, 请注意防范。\n" + detail);
                                SmsInfo info = new SmsInfo();
                                info.sender = "手动输入";
                                info.type = SmsInfo.SMS_TYPE_DECEIVE;
                                info.content = message;
                                info.datetime = Utils.getDate(Calendar.getInstance()) + "=" + Utils.getNowTime();
                                if (mDBHelper.save(info) > 0) {
                                    ToastUtil.show(context, "短信已存入历史记录！");
                                }
                            }
                        });
                    }else {
                        output_result.setText("unexpected answer!\n"+result);
                    }
                }
            });
        } catch (Exception e) {
            output_result.setText(e.getMessage());
        }
    }

    private void check(String message, int model_id) {
        try {

//            String result = ChatClient.callWithMessage(message, model_id);
            String result = chatClient.callWithMessageOnDevice(message);
            Log.d("onDevice test", "check: "+result);

            if(result.startsWith("否")) {
                output_result.setText("该短信为普通短信。");
                SmsInfo info = new SmsInfo();
                info.sender = "手动输入";
                info.type = SmsInfo.SMS_TYPE_COMMON;
                info.content = message;
                info.datetime = Utils.getDate(Calendar.getInstance())+"="+Utils.getNowTime();
                if (mDBHelper.save(info)>0){
                    ToastUtil.show(this, "短信已存入历史记录！");
                }
            }
            else if(result.startsWith("是")){
                String detail = chatClient.callForDetail(message, model_id);
                output_result.setText("该短信可能为诈骗短信, 请注意防范。\n"+detail);
                SmsInfo info = new SmsInfo();
                info.sender = "手动输入";
                info.type = SmsInfo.SMS_TYPE_DECEIVE;
                info.content = message;
                info.datetime = Utils.getDate(Calendar.getInstance())+"="+Utils.getNowTime();
                if (mDBHelper.save(info)>0){
                    ToastUtil.show(this, "短信已存入历史记录！");
                }
            }else {
                output_result.setText("unexpected answer!");
            }
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            output_result.setText(e.getMessage());
        } catch (Exception e){
            output_result.setText(e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SMS) {
            if (PermissionUtil.checkGrant(grantResults)) {
                Log.d("DEBUG", "所有权限获取成功");
                ToastUtil.show(this, "获取收发短信权限成功！");
                status = true;
                status_btn.setText(R.string.close_analysis);
                ToastUtil.show(this, "鹰眼智能识别已开启！");
            } else {
                // 部分权限获取失败
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ToastUtil.show(this, "获取收发短信权限失败！");
                        jumpToSettings();
                    }
                }
                status = false;
                status_btn.setText(R.string.open_analysis);
                ToastUtil.show(this, "鹰眼智能识别未开启！");
            }
        }
    }

    // 跳转到应用设置界面
    private void jumpToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void change_status() {
//        if (!AccessibilityServiceUtil.isAccessibilityServiceEnabled(this, SmsDetectService.class)) {
//            Log.d("DEBUG", "请在设置中手动启用辅助功能服务");
//            if(!SmsDetectService.isStart()){
//                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }

    }
}