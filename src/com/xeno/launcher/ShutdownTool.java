package com.xeno.launcher;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import com.spd.mdm.manager.MdmManager;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class ShutdownTool {
    private static ShutdownTool shutdownTool;
    public static ShutdownTool getInstance() {
        if (shutdownTool == null) {
            shutdownTool = new ShutdownTool();
        }
        return shutdownTool;
    }
    private final Timer timer;
    private TimerTask shutdownTask;
    private boolean hasPerformedWork = false;
    private boolean isWifiEnabled = false;
    static final String PROPERTY_KEY_CHECK_POWER = "check_charging";

    private boolean shutdownImpl() {
        try {
            if (hasPerformedWork) {
                MdmManager.getInstance().shutdownDevice(); // 关机
                return true;
            } else {
                if (MdmManager.getInstance().getNavigationBarEnabled()) {
                    MdmManager.getInstance().setNavigationBarEnable(false); // 禁用虚拟导航栏
                }
                if (isWifiEnabled) {
                    MdmManager.getInstance().setWifiEnable(false);
                    MdmManager.getInstance().setWifiEnable(true); // 设置 wifiEnable 为 false 之后导致无法手动开启 wifi，需要再把 wifiEnable 设置为 true
                } else if (!MdmManager.getInstance().getWifiEnabled()) {
                    MdmManager.getInstance().setWifiEnable(true);
                }
                hasPerformedWork = true;
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());
                out.writeBytes("reboot -p\n");
                out.writeBytes("exit\n");
                out.flush();
                return true;
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return false;
            }
        }
    }

    private void clearTimerTask() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
            shutdownTask = null;
        }
    }


    private ShutdownTool() {
        timer = new Timer();
    }

    void shutdown() {
        clearTimerTask();
        shutdownTask = new TimerTask() {
            @Override
            public void run() {
                if (shutdownImpl()) {
                    clearTimerTask();
                }
            }
        };
        timer.schedule(shutdownTask, 900, 1000);
    }
    void shutdown(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        isWifiEnabled = wifiManager.isWifiEnabled();
        shutdown();
    }

    boolean getCheckCharging() {
        Properties properties = PropertiesUtil.getProperties();
        boolean checkPower = true;
        if (null != properties) {
            checkPower = Boolean.parseBoolean(properties.getProperty(PROPERTY_KEY_CHECK_POWER, "true"));
        }
        return checkPower;
    }

    boolean shouldShutdown(int status) {
        return getCheckCharging() && !isCharging(status);
    }

    public static boolean isCharging(int status) {
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private boolean doClearWork(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean isWifiEnabled = wifiManager.isWifiEnabled();
        if (isWifiEnabled) {
//            return wifiManager.setWifiEnabled(false);
        }
        return isWifiEnabled;
    }
}
