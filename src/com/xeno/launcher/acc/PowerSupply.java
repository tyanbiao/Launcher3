package com.xeno.launcher.acc;

import android.util.Log;
import com.xeno.launcher.utils.ShellCommandExecutor;

/**
 *
 */
public class PowerSupply {
    static final String TAG = "PowerSupply";
    public static final String AC = "/sys/class/power_supply/ac/online";

    private final String path;

    private static final int FLAG_ON = 1;
    private static final int FLAG_OFF = 0;
    public PowerSupply(String path) {
        this.path = path;
    }
    public boolean status() {
        return cat() == FLAG_ON;
    }

    private int cat() {
        try {
            String res = ShellCommandExecutor.executeCommand("cat " + path);
            return  Integer.parseInt(res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
