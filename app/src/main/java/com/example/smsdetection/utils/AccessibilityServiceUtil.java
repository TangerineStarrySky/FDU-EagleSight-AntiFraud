package com.example.smsdetection.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class AccessibilityServiceUtil {
    private static final String TAG = "AccessibilityServiceUtil";

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        String serviceId = context.getPackageName() + "/" + serviceClass.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        boolean isEnabled = !TextUtils.isEmpty(enabledServices) && enabledServices.contains(serviceId);

        Log.d(TAG, "AccessibilityService " + serviceClass.getSimpleName() + " enabled: " + isEnabled);
        return isEnabled;
    }
}
