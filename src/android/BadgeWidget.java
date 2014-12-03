package com.xz.cordova.plugin.badge;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Badge widget class.
 *
 * @author XH
 * @version 1.0
 * @since 1.0
 * <p/>
 * Created on 2014/11/26.
 */
public class BadgeWidget extends AppWidgetProvider {

    private static final String TAG = BadgeWidget.class.getSimpleName();
    private static final String KEY_BADGE = "com.xz.cordova.plugin.badge.number";

    private int mBadgeNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get badge number from intent
        mBadgeNumber = intent.getIntExtra(KEY_BADGE, 0);

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int widgetId : appWidgetIds) {
            // get the badge number from intent
            int number = mBadgeNumber;
            int[] layoutIds = getWidgetLayoutIds(context);
            int layoutId = layoutIds[0];
            int iconId = layoutIds[1];
            int badgeId = layoutIds[2];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    layoutId);
            Log.i(TAG, String.valueOf(number));

            // Set the text
            if (number < 1) {
                remoteViews.setViewVisibility(badgeId, View.INVISIBLE);
            } else {
                remoteViews.setTextViewText(badgeId, String.valueOf(number));
                remoteViews.setViewVisibility(badgeId, View.VISIBLE);
            }

            // Register an onClickListener
            Intent intent = getMainIntent(context);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(iconId, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private Intent getMainIntent(Context context) {
        Context appContext = context.getApplicationContext();
        String pkgName = appContext.getPackageName();
        Intent intent = appContext.getPackageManager()
                .getLaunchIntentForPackage(pkgName);

        return intent;
    }

    private int[] getWidgetLayoutIds(Context context) {
        if (context == null) {
            return new int[0];
        }

        try {
            Resources res = context.getResources();
            String pkgName = context.getPackageName();
            int[] layoutIds = new int[3];
            layoutIds[0] = res.getIdentifier("widget_layout", "layout", pkgName);
            layoutIds[1] = res.getIdentifier("icon", "id", pkgName);
            layoutIds[2] = res.getIdentifier("badge", "id", pkgName);

            return layoutIds;
        } catch (Exception e) {
            throw new RuntimeException("can't get the widget layout", e);
        }
    }

    public static void setBadge(Context context, int number) {
        int[] allWidgetIds = BadgeWidget.getWidgetIds(context);
        if (number > 0) {
            Intent intent = BadgeWidget.getUpdateIntent(context, allWidgetIds);

            if (intent != null) {
                intent.putExtra(BadgeWidget.KEY_BADGE, number);

                context.sendBroadcast(intent);
            }
        } else {
            BadgeWidget.clearBadge(context);
        }
    }

    public static void clearBadge(Context context) {
        int[] allWidgetIds = BadgeWidget.getWidgetIds(context);
        Intent intent = BadgeWidget.getUpdateIntent(context, allWidgetIds);
        if (intent != null) {
            context.sendBroadcast(intent);
        }
    }

    private static int[] getWidgetIds(Context context) {
        ComponentName thisWidget = new ComponentName(context, BadgeWidget.class);
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        return mgr.getAppWidgetIds(thisWidget);
    }

    private static Intent getUpdateIntent(Context context, int[] appWidgetIds) {
        Intent intent = null;

        if (appWidgetIds.length > 0) {
            intent = new Intent(context, BadgeWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        }

        return intent;
    }
}