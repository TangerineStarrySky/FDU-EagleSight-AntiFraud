package com.example.smsdetection.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class SmsDetectService extends AccessibilityService {

    public static SmsDetectService mService;

    // 服务连上的时候回调
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取包名
        Log.d("DEBUG", "onAccessibilityEvent: "+event.toString());
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();

        Log.d("DEBUG", "eventType: " + eventType + " pkgName: " + pkgName);

        //过滤出目标包
        if (pkgName.equals("com.android.mms")) {
            Log.d("DEBUG", "eventType: " + eventType + " pkgName: " + pkgName);

            switch (eventType) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    //执行具体的脚本
                    break;
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                    // 获取短信内容
                    String smsText = event.getText().toString();
                    Log.d("DEBUG", "onAccessibilityEvent: "+smsText);
                    // 获取当前窗口的根节点
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    // 执行你的逻辑，比如读取短信内容和发信人
                    List<AccessibilityNodeInfo> nodes = new ArrayList<>();
                    traverseNodeTree(rootNode, nodes);
                    for(int i=0;i<nodes.size(); i++)
                    {
                        Log.d("NODE", "onAccessibilityEvent: "+ nodes.get(i).getText());
                    }
                    break;
            }
        }
    }

    private void traverseNodeTree(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> nodes) {
        if (node != null) {
            // 检查当前节点是否包含短信内容或发信人
            if (node.getText() != null) {
                nodes.add(node);
            }

            // 递归遍历子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                traverseNodeTree(node.getChild(i),nodes);
            }
            node.recycle(); // 重要：避免内存泄漏
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("SERVICE", "onServiceConnected: 短信实时检测服务已开启！");
//        ToastUtil.show(getApplicationContext(),"短信实时检测服务已开启！");
        mService = this;
    }

    @Override
    public void onInterrupt() {
        Log.d("SERVICE", "onInterrupt: 短信实时检测服务已中断！");
//        ToastUtil.show(getApplicationContext(),"短信实时检测服务已中断！");
        mService = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE", "onDestroy: 短信实时检测服务已关闭！");
//        ToastUtil.show(getApplicationContext(),"短信实时检测服务已关闭！");
        mService = null;
    }

    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return mService != null;
    }

}
