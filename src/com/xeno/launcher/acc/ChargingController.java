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

    /**
     * 电池充电控制
     * 尽量延长电池使用寿命，优先采用外部供电，由于重启之后配置会重置
     * 实际策略是开机之后保持充电到 max，然后停止充电，并且使用外部供电，直至电池电量到 min 再次开启充电
     * 电池电量会长期保持在 max
     * @param percent 电池电量
     * @param isCharging 电池是否在充电
     */
    public void onPowerChange(int percent, boolean isCharging) {
        if (!enable) {
            Log.d(TAG, "charging controller is disabled");
            return;
        }
        if (isCharging) {
            if (percent > max && chargingSwitch.status()) {
                chargingSwitch.off();
                Log.d(TAG, "turn off charging switch");
            }
        } else {
            if (percent < min && !chargingSwitch.status()) {
                chargingSwitch.on();
                Log.d(TAG, "turn on charging switch");
            }
        }
    }

    public boolean chargerStatus() {
        return chargingSwitch.status();
    }
}
