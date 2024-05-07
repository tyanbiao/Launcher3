package com.xeno.launcher.acc;

import android.util.Log;

import com.xeno.launcher.utils.ShellCommandExecutor;

import java.io.IOException;

public class MtkChargingSwitch implements ChargingSwitch {
    final String TAG = this.getClass().getSimpleName();
    static final String CHARGING_SWITCH_CMD = "/proc/mtk_battery_cmd/current_cmd"; // 电池充电开关
    static final String CHARGING_SWITCH_OFF = "0 1";
    static final String CHARGING_SWITCH_ON = "0 0";
    static final String POWER_PATH_CMD = "/proc/mtk_battery_cmd/en_power_path"; // 是否优先使用外部电源供电
    static final String POWER_PATH_ON = "1";

    static final String POWER_PATH_OFF = "0";

    @Override
    public void on() {
        try {
            // 开启充电，不需要设置 cmd2
            ShellCommandExecutor.executeRootCommandsWithoutResult(new String[]{
                    "echo \"" + CHARGING_SWITCH_ON + "\" >> " + CHARGING_SWITCH_CMD,
            });
        }  catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void off() {
        try {
            // 禁用电池充电，同时启用仅外部供电
            ShellCommandExecutor.executeRootCommandsWithoutResult(new String[]{
                    "echo \"" + CHARGING_SWITCH_OFF + "\" >> " + CHARGING_SWITCH_CMD,
                    "echo \"" + POWER_PATH_ON + "\" >> " + POWER_PATH_CMD
            });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean status() {
        String res = ShellCommandExecutor.executeCommand("cat " + CHARGING_SWITCH_CMD);
        return res.equals(CHARGING_SWITCH_ON);
    }
}
