package com.xeno.launcher.acc;

import android.util.Log;
import com.xeno.launcher.utils.ShellCommandExecutor;

/**
 *
 */
public class PowerSupply {
    static final String TAG = "PowerSupply";
    public static final String AC = "/sys/class/power_supply/ac/online";

    public static final String USB = "/sys/class/power_supply/usb/online";
    public static final String BATTERY_STATUS_PATH = "/sys/class/power_supply/battery/status";

    private final String[] types;

    private static final int FLAG_ON = 1;
    private static final int FLAG_OFF = 0;

    static final String BATTERY_STATUS_CHARGING = "Charging";
    static final String BATTERY_STATUS_NOT_CHARGING = "Not charging";
    static final String BATTERY_STATUS_DISCHARGING = "Discharging";
    public PowerSupply(String[] types) {
        this.types = types;
    }
    public boolean status() {
        String result = ShellCommandExecutor.executeCommand("cat " + BATTERY_STATUS_PATH);
        String content = result.trim();
        switch (content) {
            case BATTERY_STATUS_CHARGING:
            case BATTERY_STATUS_NOT_CHARGING:
                return true;
            case BATTERY_STATUS_DISCHARGING:
                return false;
            default:
                Log.d(TAG, content);
        }

        for (String type : types) {
            if (cat(type) == FLAG_ON) {
                return true;
            }
        }
        return false;
    }

    private int cat(String path) {
        try {
            String res = ShellCommandExecutor.executeCommand("cat " + path);
            return  Integer.parseInt(res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
