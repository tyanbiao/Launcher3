package com.xeno.regait;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import java.io.IOException;
import java.util.Properties;

public class LaunchTaskTool {
    static final String PROPERTIES_FILE = "application.properties";
    static final String PROPERTY_KEY_PACKAGE = "package";

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
}
