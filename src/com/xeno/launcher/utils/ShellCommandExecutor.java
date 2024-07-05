package com.xeno.launcher.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShellCommandExecutor {
    private static final String TAG = "ShellCommandExecutor";
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process;

        if (!command.endsWith("\n")) {
            command += "\n";
        }

        try {
            Log.d(TAG, command);
            process = Runtime.getRuntime().exec(command);
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return output.toString().trim();
    }

    public static String executeRootCommand(String command)  {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            if (!command.endsWith("\n")) {
                command += "\n";
            }
            Log.d(TAG, command);
            outputStream.writeBytes(command);
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
            String result = readResult(su.getInputStream());
            String error = readResult(su.getErrorStream());
            int exitValue = su.exitValue();
            if (exitValue == 0) {
                Log.d(TAG, "result: \n" + result);
                return result;
            } else {
                Log.d(TAG, "error: \n" + error);
                return error;
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public static void executeRootCommandsWithoutResult(String[] commands) throws InterruptedException, IOException {
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        for (String command : commands) {
            if (!command.endsWith("\n")) {
                command += "\n";
            }
            Log.d(TAG, command);
            outputStream.writeBytes(command);
        }
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
    }

    public static void main(String[] args) {
        String result = executeCommand("ls /");
        System.out.println(result);
    }

    private static String readResult(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int read;
        char[] buffer = new char[4096];
        StringBuffer output = new StringBuffer();
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
        }
        reader.close();
        return output.toString();
    }
}
