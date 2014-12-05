package com.xz.cordova.plugin.badge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Badge utility class.
 *
 * @author XH
 * @version 1.0
 * @since 1.0
 * <p/>
 * Created on 2014/12/5.
 */
public class BadgeUtils {
    private static final String MANUFACTURER_XIAOMI = "Xiaomi";
    private static final String MANUFACTURER_SAMSUNG = "samsung";
    private static final String MANUFACTURER_SONY = "sony";

    public static void setBadge(Context context, int number) {
        if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_XIAOMI)) {
            setBadgeXiaomi(context, number);
        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_SAMSUNG)) {
            setBadgeSamsung(context, number);
        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_SONY)) {
            setBadgeSony(context, number);
        } else {
            BadgeWidget.setBadge(context, number);
        }
    }

    public static void clearBadge(Context context) {
        if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_XIAOMI)) {
            setBadgeXiaomi(context, 0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_SAMSUNG)) {
            setBadgeSamsung(context, 0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_SONY)) {
            setBadgeSony(context, 0);
        } else {
            BadgeWidget.clearBadge(context);
        }
    }

    public static void setBadgeXiaomi(Context context, int number) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        String lancherClassSimpleName = launcherClassName.substring(
                launcherClassName.lastIndexOf(".") + 1);

        try {
            // After MIUI 6.0+.
            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
            Object miuiNotification = miuiNotificationClass.newInstance();
            Field field = miuiNotification.getClass().getDeclaredField("messageCount");
            field.setAccessible(true);
            field.set(miuiNotification, number);
        } catch (Exception e) {
            // Before MIUI 6.0.
            Intent localIntent = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
            localIntent.putExtra("android.intent.extra.update_application_component_name", context.getPackageName() + "/." + lancherClassSimpleName);
            localIntent.putExtra("android.intent.extra.update_application_message_text", number > 0 ? String.valueOf(number) : "");
            context.sendBroadcast(localIntent);
        }
    }

    public static void setBadgeSamsung(Context context, int number) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent localIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        localIntent.putExtra("badge_count", number);
        localIntent.putExtra("badge_count_package_name", context.getPackageName());
        localIntent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(localIntent);
    }

    public static void setBadgeSony(Context context, int number) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent localIntent = new Intent();
        localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", number > 0);
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",launcherClassName);
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(number));
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
        context.sendBroadcast(localIntent);
    }

    private static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }

        return null;
    }
}
