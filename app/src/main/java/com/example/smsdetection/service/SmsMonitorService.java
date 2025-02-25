package com.example.smsdetection.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.smsdetection.R;

public class SmsMonitorService extends Service {
    private static final String CHANNEL_ID = "SmsDetectServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("短信监控运行中")
                .setContentText("鹰眼正在后台监控短信...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        // 启动前台服务
        startForeground(1, notification);

        // 注册ContentObserver监听短信数据库变化
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms"),
                true,
                new SmsGetObserver(this)
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(new SmsGetObserver(this));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sms Detect Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
