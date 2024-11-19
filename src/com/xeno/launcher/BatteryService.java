package com.xeno.launcher;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.launcher3.BuildConfig;
import com.xeno.launcher.acc.ChargingController;
import com.xeno.launcher.acc.MtkChargingSwitch;
import com.xeno.launcher.acc.PowerSupply;
import com.xeno.launcher.utils.ServiceUtil;

import java.util.Objects;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryService extends Service {
    private static final int BATTERY_SERVICE_ID = 0x17;

    private static BroadcastReceiver powerReceiver;
    private static final String TAG = "BatteryService";

    private final Timer timer;
    private final int POWER_SUPPLY_DELAY;
    private TimerTask task = null;

    private final PowerSupply powerSupply = new PowerSupply(new String[] { PowerSupply.AC, PowerSupply.USB });

    private final ChargingController chargingController;
    public BatteryService() {
        timer = new Timer();
        PropertiesUtil.initPropertiesFile(this);
        Properties properties = PropertiesUtil.getProperties();
        int defaultDelay = 1100;
        if (properties != null) {
            POWER_SUPPLY_DELAY = Integer.parseInt(properties.getProperty(PropertiesUtil.CHECK_CHARGING_DELAY, String.valueOf(defaultDelay)));
        } else {
            POWER_SUPPLY_DELAY = defaultDelay;
        }
        chargingController = new ChargingController(new MtkChargingSwitch());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        registerPowerReceiver();
        boolean isPowerSupplyOnline = powerSupply.status();
        Log.d(TAG, "isPowerSupplyOnline="+isPowerSupplyOnline);
        if (!isPowerSupplyOnline) {
            onAcChargingDisconnected();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = ServiceUtil.creatNotification(this, BatteryService.class);
            startForeground(BATTERY_SERVICE_ID, notification);
        }
        return START_STICKY;
    }

    private void registerPowerReceiver() {
        if (powerReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(powerReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        powerReceiver = new PowerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(powerReceiver, filter);
        Log.d(TAG, "registerPowerReceiver,batteryStatus==null " + (batteryStatus == null));
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "onDestroy");
        if (powerReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(powerReceiver);
                powerReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    void  onAcChargingDisconnected() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "AC Charging Disconnected");
                if (ShutdownTool.getInstance().getCheckCharging()) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean willCloseWifi = wifiManager != null && wifiManager.isWifiEnabled();
                    ShutdownTool.getInstance().shutdown(willCloseWifi);
                    // 发送广播
                    String ACTION_AC_DISCONNECTED = BuildConfig.APPLICATION_ID + ".AC_DISCONNECTED";
                    Intent intent = new Intent(ACTION_AC_DISCONNECTED);
                    intent.putExtra("charger_status", chargingController.chargerStatus());
                    sendBroadcast(intent);
                }
                task = null;
            }
        };
        timer.schedule(task, POWER_SUPPLY_DELAY);
    }

    void onAcChargingConnected() {
        if (task != null) {
            task.cancel();
            task = null;
            return;
        }
        Log.d(TAG, "AC Charging Connected");
        if (ShutdownTool.getInstance().getCheckCharging()) {
            String ACTION_AC_CONNECTED = BuildConfig.APPLICATION_ID + ".AC_CONNECTED";
            Intent intent = new Intent(ACTION_AC_CONNECTED);
            intent.putExtra("charger_status", chargingController.chargerStatus());
            sendBroadcast(intent);
        }
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

            switch (Objects.requireNonNull(intent.getAction())) {
                // 只有电源插上时才会发送此事件，主动充电不会触发
                case Intent.ACTION_POWER_CONNECTED:
                    onAcChargingConnected();
                    break;
                // 只有外部电源断开时才会发送此事件，主动停充不会触发
                case Intent.ACTION_POWER_DISCONNECTED:
                    onAcChargingDisconnected();
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    chargingController.onBatteryChanged(batteryPercentage, isCharging);
                    break;
            }
        }
    }
}
