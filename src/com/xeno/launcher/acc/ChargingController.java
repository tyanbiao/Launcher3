package com.xeno.launcher.acc;

import android.util.Log;

import com.xeno.launcher.PropertiesUtil;

import java.util.Properties;

public class ChargingController {
    final String TAG = ChargingController.class.getSimpleName();
    private ChargingSwitch chargingSwitch;

    private int min = 20;
    private int max = 80;
    private boolean enable = true;

    public ChargingController(ChargingSwitch chargingSwitch) {
        this.chargingSwitch = chargingSwitch;
        Properties properties = PropertiesUtil.getProperties();
        if (null != properties) {
            enable = Boolean.parseBoolean(properties.getProperty(PropertiesUtil.CHARGING_CONTROLLER_ENABLED, "true"));
            min = Integer.parseInt(properties.getProperty(PropertiesUtil.CHARGING_CONTROLLER_MIN, "20"));
            max = Integer.parseInt(properties.getProperty(PropertiesUtil.CHARGING_CONTROLLER_MAX, "80"));
        }
    }

    public void onPowerChange(int percent, boolean isCharging) {
        if (!enable) {
            Log.d(TAG, "charging controller is disabled");
            return;
        }
        if (isCharging) {
            if (percent > max && chargingSwitch.status()) {
                Log.d(TAG, "turn off charger");
                chargingSwitch.off();
            }
        } else {
            if (percent < min && !chargingSwitch.status()) {
                Log.d(TAG, "turn on charger");
                chargingSwitch.on();
            }
        }
    }

    public boolean chargerStatus() {
        return chargingSwitch.status();
    }
}
