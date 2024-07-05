package com.xeno.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import com.android.launcher3.R;
import java.io.IOException;
import java.util.Properties;

public class LaunchTaskTool {
    static final String TAG = "LaunchTaskTool";

    private static void startPackage(Activity activity) {
        String packageName = getPackageName(activity);
        if (packageName == null) {
            return;
        }
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        activity.startActivity(intent);
    }

    private static String getPackageName(Activity activity) {
        try {
            Properties properties = PropertiesUtil.getProperties();
            String packageName = null;
            if (properties != null) {
                packageName = properties.getProperty(PropertiesUtil.START_PACKAGE, null);
            }
            if (packageName == null || packageName.length() == 0) {
                return null;
            }

            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(packageName, 0);

            if (packageInfo == null) {
                return null;
            }

            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void hideSystemUI(Activity activity) {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
//                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
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

    private static void setWallPaper(final Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean hasSetWallpaper = sharedPref.getBoolean(activity.getString(R.string.save_wallpaper_has_init_key), false);
        if (hasSetWallpaper) {
            return;
        }

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity.getApplicationContext());

        try {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;
            Bitmap bmap2 = BitmapFactory.decodeResource(activity.getResources(), R.drawable.wallpaper);
            Bitmap bitmap = Bitmap.createScaledBitmap(bmap2, width, height, true);
            wallpaperManager.setBitmap(bitmap);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(activity.getString(R.string.save_wallpaper_has_init_key), true);
            editor.apply();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onLauncherStart(Activity activity) throws IOException {
        Log.d(TAG, "Launcher start");
        PropertiesUtil.initPropertiesFile(activity);
        startPackage(activity);
        setWallPaper(activity);
    }
}
