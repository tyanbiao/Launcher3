package com.xeno.launcher;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.spd.mdm.manager.MdmManager;
import com.xeno.launcher.utils.ShellCommandExecutor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class ShutdownTool {
    static final String TAG = "ShutdownTool";
    static final int OFF_TYPE_REBOOT = 1;
    static final int OFF_TYPE_POWER_SHUTDOWN = 2;

    static final int OFF_TYPE_SPD_SHUTDOWN = 3;
    private int offType = OFF_TYPE_REBOOT;
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
    private boolean willCloseWifi = false;

    private boolean shutdownImpl() {
        try {
            if (hasPerformedWork) {
                switch (offType) {
                    case OFF_TYPE_REBOOT:
                        ShellCommandExecutor.executeRootCommand("reboot -p");
                        break;
                    case OFF_TYPE_POWER_SHUTDOWN:
                        ShellCommandExecutor.executeRootCommand("svc power shutdown");
                        break;
                    case OFF_TYPE_SPD_SHUTDOWN:
                        MdmManager.getInstance().shutdownDevice(); // SD55 关机
                        break;
                }
                return true;
            } else {
                if (Build.VERSION.SDK_INT < 30) {
                    if (MdmManager.getInstance().getNavigationBarEnabled()) {
                        MdmManager.getInstance().setNavigationBarEnable(false); // 禁用虚拟导航栏
                    }
                    if (willCloseWifi) {
                        // 关闭 WiFi
                        ShellCommandExecutor.executeRootCommand("svc wifi disable");

//                        MdmManager.getInstance().setWifiEnable(false);
//                        MdmManager.getInstance().setWifiEnable(true); // 设置 wifiEnable 为 false 之后导致无法手动开启 wifi，需要再把 wifiEnable 设置为 true
                    } else if (!MdmManager.getInstance().getWifiEnabled()) {
                        MdmManager.getInstance().setWifiEnable(true);
                    }
                    offType = OFF_TYPE_SPD_SHUTDOWN;
                } else {
                    if (willCloseWifi) {
                        ShellCommandExecutor.executeRootCommand("svc wifi disable");
                    }
                    String ret = ShellCommandExecutor.executeRootCommand("pm list packages -d | grep com.android.systemui");
                    Log.d(TAG, ret);
                    if (ret != null && ret.contains("com.android.systemui")) {
                        // 已经禁用 systemui
                        offType = OFF_TYPE_REBOOT;
                    } else {
                        // 未禁用 systemui
                        ShellCommandExecutor.executeRootCommand("pm disable com.android.systemui");
                        offType = OFF_TYPE_POWER_SHUTDOWN;
                    }
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

    void shutdown(boolean willCloseWifi) {
        this.willCloseWifi = willCloseWifi;
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
    void shutdown() {
        shutdown(false);
    }

    boolean getCheckCharging() {
        Properties properties = PropertiesUtil.getProperties();
        boolean checkPower = true;
        if (null != properties) {
            checkPower = Boolean.parseBoolean(properties.getProperty(PropertiesUtil.CHECK_POWER, "true"));
        }
        return checkPower;
    }
}
