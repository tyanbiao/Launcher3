package com.xeno.launcher;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    static final String TAG = "PropertiesUtil";

    public static Properties getProperties() {
        String root = getInternalStorageRootPath();
        if (root == null) {
            return null;
        }
        String filepath = root + "/xeno/regait/config.properties";
        File file = new File(filepath);
        return getProperties(file);
    }
    public static Properties getProperties(File file) {
        try {
            Properties temp = new Properties();
            if (!file.exists()) {
                boolean res = file.createNewFile();
                if (!res) {
                    return null;
                }
            }
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            temp.load(in);
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"获取配置文件失败{0}", e);
            return null;
        }
    }

    public static String getInternalStorageRootPath() {
        if (!isExternalStorageAvailable()) {
            Log.e(TAG, "内部存储不可用");
            return null;
        }
        return Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
    }

    // 检查外部存储是否可读可写
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static  boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
