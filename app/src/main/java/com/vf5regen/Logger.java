package com.vf5regen;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    private static final String FILE_NAME = "vf5_regen_debug.log";
    private static File logFile;

    public static void init(Context context) {
        File dir = context.getExternalFilesDir(null);
        if (dir != null) {
            logFile = new File(dir, FILE_NAME);
            log("--- Logger Initialized at " + getTimestamp() + " ---");
        }
    }

    public static synchronized void log(String message) {
        if (logFile == null) return;
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(getTimestamp() + " " + message + "\n");
            osw.close();
            fos.close();
        } catch (Exception e) {
            Log.e("VF5Logger", "Failed to write log", e);
        }
    }

    private static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
    }

    public static String getLogPath() {
        return logFile != null ? logFile.getAbsolutePath() : "Unknown";
    }
}
