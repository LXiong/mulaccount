package io.virtualapp.util;

import android.util.Log;

import io.virtualapp.BuildConfig;

/**
 * Created by IVY on 2016/10/8.
 */

public class LogUtils {

    public static void i(String tag, String msg, Object... format) {
        if (BuildConfig.PRINT_LOG) {
            Log.i(tag, String.format(msg, format));
        }
    }

    public static void d(String tag, String msg, Object... format) {
        if (BuildConfig.PRINT_LOG) {
            Log.d(tag, String.format(msg, format));
        }
    }

    public static void w(String tag, String msg, Object... format) {
        if (BuildConfig.PRINT_LOG) {
            Log.w(tag, String.format(msg, format));
        }
    }

    public static void e(String tag, String msg, Object... format) {
        if (BuildConfig.PRINT_LOG) {
            Log.e(tag, String.format(msg, format));
        }
    }

    public static void v(String tag, String msg, Object... format) {
        if (BuildConfig.PRINT_LOG) {
            Log.v(tag, String.format(msg, format));
        }
    }

    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    public static void printStackTrace(String tag) {
        Log.e(tag, getStackTraceString(new Exception()));
    }

    public static void e(String tag, Throwable e) {
        Log.e(tag, getStackTraceString(e));
    }
}
