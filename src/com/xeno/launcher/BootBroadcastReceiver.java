package com.xeno.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = "BootBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "启动成功");
            Intent batteryBroadcast = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryBroadcast != null) {
                int status = batteryBroadcast.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (ShutdownTool.getInstance().shouldShutdown(status)) {
                    ShutdownTool.getInstance().shutdown();
                }
            }
        }
    }
}
