package com.xeno.launcher.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;

import java.util.List;

public class ServiceUtil {
    public static final String ChannelId = BuildConfig.APPLICATION_ID + ".CHANNEL_DEFAULT_IMPORTANCE";
    public static final String ChannelName = "MonitorMainChannel";

    public static boolean checkServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;
        List<ActivityManager.RunningServiceInfo> infoList = manager.getRunningServices(Integer.MAX_VALUE);
        if (infoList == null || infoList.isEmpty()) return false;

        for (ActivityManager.RunningServiceInfo info : infoList) {
            // 添加Uid验证, 防止服务重名, 当前服务无法启动
            if (serviceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.setSound(null, null);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification creatNotification(Context context, Class<?> clzz) {
        Intent notificationIntent = new Intent(context, clzz);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        createNotificationChannel(context, ChannelId, ChannelName);

        return new Notification.Builder(context, ChannelId)
                .setContentTitle("BatteryService is running")
                .setSmallIcon(R.drawable.ic_battery)
                .setColor(0x001F8DD1)
                .setContentIntent(pendingIntent)
                .build();
    }
}
