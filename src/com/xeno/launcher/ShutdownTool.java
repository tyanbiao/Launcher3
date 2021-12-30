package com.xeno.launcher;

import android.os.BatteryManager;
import com.spd.mdm.manager.MdmManager;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private boolean checkCharging = true;
    private final Timer timer;
    private TimerTask shutdownTask;

    private boolean shutdownImpl() {
        try {
            if (MdmManager.getInstance().getNavigationBarEnabled()) {
                MdmManager.getInstance().setNavigationBarEnable(false); // 禁用虚拟导航栏
            }
//            if (MdmManager.getInstance().getStatusBarPullEnabled()) {
//                MdmManager.getInstance().setStatusBarPullEnable(false);  // 禁止状态栏下拉
//            }
            MdmManager.getInstance().shutdownDevice(); // 关机
            return true;
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

    void setCheckCharging(boolean val) {
        this.checkCharging = val;
    }

    boolean getCheckCharging() {
        return checkCharging;
    }

    boolean shouldShutdown(int status) {
        return checkCharging && !isCharging(status);
    }

    public static boolean isCharging(int status) {
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
