package com.example.smsdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.smsdetection.utils.ChatClient;

public class SmsDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private SmsDBHelper mDBHelper;
    private TextView detail_datetime;
    private TextView detail_sender;
    private TextView detail_content;
    private TextView detail_result;
    private TextView detail_type;
    private Button detail_btn;

    private AppViewModel.ChatState chatState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("短信详情");
        TextView tv_history = findViewById(R.id.tv_history);
        tv_history.setText("历史记录");

        findViewById(R.id.tv_history).setOnClickListener(this);
        findViewById(R.id.ic_back).setOnClickListener(this);
        detail_btn = findViewById(R.id.detail_btn);
        detail_btn.setOnClickListener(this);

        detail_datetime = findViewById(R.id.detail_datetime);
        detail_sender = findViewById(R.id.detail_sender);
        detail_content = findViewById(R.id.detail_content);
        detail_result = findViewById(R.id.detail_result);
        detail_type = findViewById(R.id.detail_type);

        mDBHelper = SmsDBHelper.getInstance(this);

        // 11.30
//        chatState = (AppViewModel.ChatState) getIntent().getSerializableExtra("chat_state");
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if(vid == R.id.ic_back){
            finish();
        }else if (vid == R.id.tv_history){
            Intent intent = new Intent();
            intent.setClass(SmsDetailActivity.this, HistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(vid == R.id.detail_btn){
//            String detail = null;
//            try {
//                detail = ChatClient.callForDetail(String.valueOf(detail_content.getText()), ChatClient.QWEN1_5b);
//            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
//                detail = e.getMessage();
//            }
            chatState.myChat(String.valueOf(detail_content.getText()), true, new ChatCallback() {
                @Override
                public void onResult(@NonNull String detail) {
                    detail_result.setText(detail);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDetail();
    }

    private void showDetail() {
        // 获取上一个页面传来的编号
        int sms_id = getIntent().getIntExtra("sms_id", 0);
        if (sms_id > 0) {
            // 根据商品编号查询商品数据库中的商品记录
            SmsInfo info = mDBHelper.querySmsInfoById(sms_id);
            String[] strs = info.datetime.split("=");
            detail_datetime.setText(strs[0]+" "+strs[1]);
            detail_sender.setText(info.sender);
            detail_content.setText(info.content);
            if(info.type == 1){
                detail_btn.setVisibility(View.VISIBLE);
                detail_result.setVisibility(View.VISIBLE);
                detail_type.setTextColor(getResources().getColor(R.color.red));
                detail_type.setText("该短信可能为诈骗短信。");
            }else {
                detail_type.setTextColor(getResources().getColor(R.color.green));
                detail_type.setText("该短信为普通短信。");
            }
        }
    }
}