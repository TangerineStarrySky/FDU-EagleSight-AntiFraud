package com.example.smsdetection.entity;

public class SmsInfo {

    public int id;
    public String datetime;
    public String sender;
    public String content;
    public int type;

    // 账单类型，0 收入，1 支出
    public static final int SMS_TYPE_COMMON = 0;
    public static final int SMS_TYPE_DECEIVE = 1;

    @Override
    public String toString() {
        return "SmsInfo{" +
                "id=" + id +
                ", datetime='" + datetime + '\'' +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                '}';
    }
}
