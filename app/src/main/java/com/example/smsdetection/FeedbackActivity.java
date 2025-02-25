package com.example.smsdetection;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.smsdetection.utils.ToastUtil;

public class FeedbackActivity extends AppCompatActivity {

    private EditText etFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFeedback = findViewById(R.id.et_feedback);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("用户反馈");
        TextView tv_history = findViewById(R.id.tv_history);
        tv_history.setText("");

        findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btn_submit_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        String feedback = etFeedback.getText().toString();
        // 这里应该添加将反馈发送到服务器或保存到本地的代码
        // 例如，使用Firebase、电子邮件或其他后端服务
        if (feedback.isBlank()) {
            // 提示用户输入反馈内容
            ToastUtil.show(this, "请输入反馈内容");
            return;
        }

        // 创建发送邮件的Intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "用户反馈");
        emailIntent.putExtra(Intent.EXTRA_TEXT, feedback);

        // 尝试启动发送邮件的Intent
        try {
            startActivity(Intent.createChooser(emailIntent, "发送反馈..."));
        } catch (android.content.ActivityNotFoundException ex) {
            // 没有处理邮件的应用程序
             ToastUtil.show(this, "没有找到发送邮件的应用程序");
        }

        // 显示提交成功的提示
        ToastUtil.show(this, "感谢您的反馈~");
        // 完成后关闭活动
        finish();
    }
}
