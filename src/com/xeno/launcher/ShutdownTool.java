package com.xeno.launcher;

import android.annotation.SuppressLint;
import android.os.BatteryManager;

import com.spd.mdm.manager.MdmManager;

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
    private Timer timer;
    private final TimerTask shutdownTask = new TimerTask() {
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            try {
                MdmManager.getInstance().shutdownDevice(); // 关机
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private ShutdownTool() {}

    void shutdown() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
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
