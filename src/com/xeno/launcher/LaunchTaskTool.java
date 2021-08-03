package com.xeno.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.Properties;

public class LaunchTaskTool {
    static final String PROPERTIES_FILE = "application.properties";
    static final String PROPERTY_KEY_PACKAGE = "package";
    static final String PROPERTY_KEY_CHECK_POWER = "check_power";
    static final String TAG = LaunchTaskTool.class.getSimpleName();

    public static void startPackage(Activity activity) {
        String packageName = getPackageName(activity);
        if (packageName != null) {
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
            activity.startActivity(intent);
        }
    }

    private static String getPackageName(Activity activity) {
        try {
            Properties properties = new Properties();
            properties.load(activity.getAssets().open(PROPERTIES_FILE));
            String packageName = properties.getProperty(PROPERTY_KEY_PACKAGE, null);
            if (packageName == null || packageName.length() == 0) {
                return null;
            }

            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(packageName, 0);

            if (packageInfo == null) {
                return null;
            }

            return packageInfo.packageName;
        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void hideSystemUI(Activity activity) {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    public static void showSystemUI(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void initReceiver(Context context) throws IOException {
        Properties properties = new Properties();
        properties.load(context.getAssets().open(PROPERTIES_FILE));
        boolean checkPower = Boolean.parseBoolean(properties.getProperty(PROPERTY_KEY_CHECK_POWER, "false"));
        PowerReceiver powerReceiver = new PowerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(powerReceiver, filter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        Log.d(TAG, "checkPower = " + checkPower);

        if (checkPower && !isCharging) {
            powerReceiver.shutdown();
        }
    }


}
