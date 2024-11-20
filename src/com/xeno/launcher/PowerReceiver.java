package com.xeno.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.launcher3.BuildConfig;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PowerReceiver extends BroadcastReceiver {
    final String TAG = "PowerReceiver";
    private final Timer timer;
    private TimerTask task = null;
    private final int POWER_OFF_DELAY = 5000;
    static final String POWER_PREFERENCE_FILE_KEY = BuildConfig.APPLICATION_ID + ".power_preference";
    static final String PREFERENCE_KEY_POWER_OFF = "power_off";

    public PowerReceiver() {
        timer = new Timer();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action="+intent.getAction());
        // 作为备选，确保控制器一定能关机
        switch (Objects.requireNonNull(intent.getAction())) {
            // 只有电源插上时才会发送此事件，主动充电不会触发
            case Intent.ACTION_POWER_CONNECTED:
                onPowerConnected(context.getApplicationContext());
                break;
            // 只有外部电源断开时才会发送此事件，主动停充不会触发
            case Intent.ACTION_POWER_DISCONNECTED:
                onPowerDisconnected(context.getApplicationContext());
                break;
        }
    }

    void onPowerDisconnected(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(POWER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_POWER_OFF, true);
        editor.commit();
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = new TimerTask() {
            @Override
            public void run() {
                if (!sp.getBoolean(PREFERENCE_KEY_POWER_OFF, false)) {
                    return;
                }
                if (ShutdownTool.getInstance().getCheckCharging()) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean willCloseWifi = wifiManager != null && wifiManager.isWifiEnabled();
                    ShutdownTool.getInstance().shutdown(willCloseWifi);
                }
                task = null;
            }
        };
        timer.schedule(task, POWER_OFF_DELAY);
    }

    void onPowerConnected(final Context context) {
        SharedPreferences sp = context.getSharedPreferences(POWER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        boolean powerOff = sp.getBoolean(PREFERENCE_KEY_POWER_OFF, false);
        if (!powerOff) {
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_POWER_OFF, false);
        editor.commit();
    }
}
