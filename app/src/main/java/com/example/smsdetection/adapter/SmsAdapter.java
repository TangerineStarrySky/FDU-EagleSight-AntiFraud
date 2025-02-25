package com.example.smsdetection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.smsdetection.R;
import com.example.smsdetection.entity.SmsInfo;

import java.util.List;

public class SmsAdapter extends BaseAdapter {

    private Context mContext;
    private List<SmsInfo> mSmsList;

    public SmsAdapter(Context mContext, List<SmsInfo> mSmsList) {
        this.mContext = mContext;
        this.mSmsList = mSmsList;
    }

    @Override
    public int getCount() {
        return mSmsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSmsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            // 获取布局文件item_cart.xml的根视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_sms, null);
            holder.item_date = convertView.findViewById(R.id.item_date);
            holder.item_time = convertView.findViewById(R.id.item_time);
            holder.item_sender = convertView.findViewById(R.id.item_sender);
            holder.item_content = convertView.findViewById(R.id.item_content);
            holder.item_type = convertView.findViewById(R.id.item_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SmsInfo info = mSmsList.get(position);
        String[] strs = info.datetime.split("=");
        holder.item_date.setText(strs[0]);
        holder.item_time.setText(strs[1]);
        holder.item_sender.setText(info.sender.length() < 14? info.sender:info.sender.substring(0,14)+"……");
        holder.item_content.setText(info.content.length() < 46? info.content:info.content.substring(0,46)+"……");
        holder.item_type.setText(info.type==1?"诈骗":"普通");
        holder.item_type.setTextColor(info.type==1?convertView.getResources().getColor(R.color.red):convertView.getResources().getColor(R.color.green));
        return convertView;
    }

    public final class ViewHolder {
        public TextView item_date;
        public TextView item_time;
        public TextView item_sender;
        public TextView item_content;
        public TextView item_type;
    }
}
