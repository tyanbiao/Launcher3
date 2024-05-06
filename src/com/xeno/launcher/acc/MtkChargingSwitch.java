package com.xeno.launcher.acc;

import android.util.Log;

import com.xeno.launcher.utils.ShellCommandExecutor;

import java.io.IOException;

public class MtkChargingSwitch implements ChargingSwitch {
    final String TAG = this.getClass().getSimpleName();
    static final String cmd1 = "/proc/mtk_battery_cmd/current_cmd";
    static final String cmd1Off = "0 1";
    static final String cmd1On = "0 0";
    static final String cmd2 = "/proc/mtk_battery_cmd/en_power_path";
    static final String cmd2On = "1";

    static final String cmd2Off = "0";

    @Override
    public void on() {
        try {
            ShellCommandExecutor.executeRootCommandsWithoutResult(new String[]{
                    "echo \"" + cmd1On + "\" >> " + cmd1,
                    "echo \"" + cmd2On + "\" >> " + cmd2
            });
        }  catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void off() {
        try {
            ShellCommandExecutor.executeRootCommandsWithoutResult(new String[]{
                    "echo \"" + cmd1Off + "\" >> " + cmd1,
                    "echo \"" + cmd2Off + "\" >> " + cmd2
            });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean status() {
        String res = ShellCommandExecutor.executeCommand("cat " + cmd1);
        return res.equals(cmd1On);
    }
}
