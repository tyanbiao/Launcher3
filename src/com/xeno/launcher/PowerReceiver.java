package com.xeno.launcher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.spd.mdm.manager.MdmManager;


import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_POWER_CONNECTED:
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                onPowerDisconnected(context, intent);
                break;
        }
    }

    void  onPowerDisconnected(Context context, Intent intent) {
        if (ShutdownTool.getInstance().getCheckCharging()) {
            ShutdownTool.getInstance().shutdown(context);
        }
    }
}
