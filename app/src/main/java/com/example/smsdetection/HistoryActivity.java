package com.example.smsdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smsdetection.adapter.SmsAdapter;
import com.example.smsdetection.database.SmsDBHelper;
import com.example.smsdetection.entity.SmsInfo;
//import com.example.smsdetection.model.AppViewModel;
import com.example.smsdetection.utils.ToastUtil;

import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private SmsDBHelper mDBHelper;
    private List<SmsInfo> mSmsList;
    private SmsAdapter mSmsAdapter;
    private ListView lv_sms;
    private TextView tv_total_num;

    private EditText etSearch;
    private TextView tv_no_sms;
    private LinearLayout bottom_layout;


//    private AppViewModel.ChatState chatState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mDBHelper = SmsDBHelper.getInstance(this);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("历史记录");
        TextView tv_history = findViewById(R.id.tv_history);
        tv_history.setText("");

        findViewById(R.id.ic_back).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_statistics).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);

        lv_sms = findViewById(R.id.lv_sms);
        tv_total_num = findViewById(R.id.tv_total_num);
        etSearch = findViewById(R.id.et_search);
        tv_no_sms = findViewById(R.id.tv_no_sms);
        bottom_layout = findViewById(R.id.bottom_layout);

        mSmsList = mDBHelper.queryAllSmsInfo();
        showSMS(mSmsList);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if(vid == R.id.ic_back){
            finish();
        } else if (vid == R.id.btn_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
            builder.setMessage("确定要清空所有信息记录？");
            builder.setPositiveButton("是", (dialog, which) -> {
                mDBHelper.deleteAllSmsInfo();
                mSmsList.clear();
                refreshTotalNum();
                mSmsAdapter.notifyDataSetChanged();
                ToastUtil.show(this, "历史信息已清空");
            });
            builder.setNegativeButton("否", null);
            builder.create().show();
        }else if(vid == R.id.btn_statistics){
            Intent intent = new Intent(HistoryActivity.this, StatisticsActivity.class);
            startActivity(intent);
        } else if (vid == R.id.btn_search) {
            String keyword = etSearch.getText().toString().trim();
            Log.d("DEBUG", keyword);
            mSmsList = mDBHelper.querySmsInfoByContent(keyword);
            Log.d("DEBUG", String.valueOf(mSmsList.size()));
            if(mSmsList.isEmpty()) tv_no_sms.setText("没有符合要求的短信记录!");
            showSMS(mSmsList);
        }
    }


    private void showSMS(List<SmsInfo> mSmsList) {
        Collections.reverse(mSmsList);
        if (mSmsList.isEmpty()) {
            tv_no_sms.setVisibility(View.VISIBLE);
            lv_sms.setVisibility(View.GONE);
            bottom_layout.setVisibility(View.GONE);
            return;
        }
        tv_no_sms.setVisibility(View.GONE);
        lv_sms.setVisibility(View.VISIBLE);
        bottom_layout.setVisibility(View.VISIBLE);
        mSmsAdapter = new SmsAdapter(this, mSmsList);
        lv_sms.setAdapter(mSmsAdapter);
        // 给列表项设置监听
        lv_sms.setOnItemClickListener(this);
        lv_sms.setOnItemLongClickListener(this);
        // 重新计算总数
        refreshTotalNum();
    }

    private void refreshTotalNum() {
        tv_total_num.setText(String.valueOf(mSmsList.size()));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent(HistoryActivity.this, SmsDetailActivity.class);
        intent.putExtra("sms_id", mSmsList.get(position).id);
//        intent.putExtra("chat_state", chatState);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        SmsInfo info = mSmsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
        builder.setMessage("是否删除此条信息？");
        builder.setPositiveButton("是", (dialog, which) -> {
            // 删除该商品
            mDBHelper.deleteSmsInfoById(info.id);
            mSmsList.remove(position);
            // 通知适配器发生了数据变化
            mSmsAdapter.notifyDataSetChanged();
            // 刷新总数
            refreshTotalNum();
            ToastUtil.show(this, "已删除该信息！");
        });
        builder.setNegativeButton("否", null);
        builder.create().show();
        return true;
    }
}