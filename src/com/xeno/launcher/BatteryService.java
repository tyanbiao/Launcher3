package com.xeno.launcher;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.launcher3.BuildConfig;
import com.xeno.launcher.acc.ChargingController;
import com.xeno.launcher.acc.MtkChargingSwitch;
import com.xeno.launcher.utils.ShellCommandExecutor;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryService extends Service {
    private static BroadcastReceiver powerReceiver;
    private final Timer timer;
    private TimerTask checkChargingTask;
    private static final String TAG = "BatteryService";

    private final ChargingController chargingController = new ChargingController(new MtkChargingSwitch());
    public BatteryService() {
        timer = new Timer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        registerPowerReceiver();
        return START_STICKY;
    }

    private void startCheckAcCharger() {
        stopCheckAcCharger();
        checkChargingTask = new TimerTask() {
            @Override
            public void run() {
                boolean res = isAcChargerOnline();
                Log.d(TAG, "isAcChargerOnline=" + res);
                if (!res) {
                    stopCheckAcCharger();
                    onAcChargerDisconnected();
                }
            }
        };
        timer.schedule(checkChargingTask, 1500, 1500);
    }

    private void stopCheckAcCharger() {
        if (checkChargingTask != null) {
            checkChargingTask.cancel();
            checkChargingTask = null;
        }
    }

    private void registerPowerReceiver() {
        if (powerReceiver != null) {
            unregisterReceiver(powerReceiver);
        }
        powerReceiver = new PowerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(powerReceiver, filter);
        Log.d(TAG, "registerPowerReceiver,batteryStatus==null " + (batteryStatus == null));
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            Log.d(TAG, "isCharging=" + isCharging);
            if (!isCharging) {
                startCheckAcCharger();
            }
        }
    }

    private boolean isAcChargerOnline() {
        try {
            String cmd = "cat /sys/class/power_supply/ac/online\n";
//            String cmd2 = "dumpsys battery | egrep 'powered: true'\n";
            String res = ShellCommandExecutor.executeCommand(cmd);
            return  res.equals("1");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "onDestroy");
        if (powerReceiver != null) {
            try {
                unregisterReceiver(powerReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    void  onAcChargerDisconnected() {
        if (ShutdownTool.getInstance().getCheckCharging()) {
            ShutdownTool.getInstance().shutdown(this);
        }
        // 发送广播
        String ACTION_AC_DISCONNECTED = BuildConfig.APPLICATION_ID + ".AC_DISCONNECTED";
        Intent intent = new Intent(ACTION_AC_DISCONNECTED);
        intent.putExtra("charger_status", chargingController.chargerStatus());
        sendBroadcast(intent);
    }

    public class PowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            // 获取电池的最大电量
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            // 计算电池电量百分比
            int batteryPercentage = 100 * level / scale;
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            Log.d(TAG, "action="+intent.getAction()+",temperature="+temperature+",batteryPercentage="+batteryPercentage+",isCharging="+isCharging);

            chargingController.onPowerChange(batteryPercentage, isCharging);

            switch (Objects.requireNonNull(intent.getAction())) {
                // 电池开始充电，控制器一定正常上电
                case Intent.ACTION_POWER_CONNECTED:
                    stopCheckAcCharger();
                    break;
                // 电池停止充电，检查是否是主动停充还是控制器掉电
                case Intent.ACTION_POWER_DISCONNECTED:
                    startCheckAcCharger();
                    break;
            }
        }
    }
}
