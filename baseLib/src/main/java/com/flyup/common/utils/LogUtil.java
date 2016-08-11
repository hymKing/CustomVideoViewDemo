package com.flyup.common.utils;

import android.util.Log;

/**
 * log 工具类
 *
 * @author solo
 */
public class LogUtil {
    public static boolean DEBUG = true;
    public final static String PREFIX = "";//gg


    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(PREFIX + tag, msg);
        }
    }


    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(PREFIX + tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(PREFIX + tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(PREFIX + tag, msg);
        }
    }

    public static void e(String tag, String msg, Object... obj) {
        if (DEBUG) {
            if (obj != null) {
                int index = 1;
                for (Object o : obj) {
                    Log.e(PREFIX + tag, index++ + " msg:" + msg + " detail: " + o);
                }
            }
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.e(PREFIX + tag, msg, tr);
        }
    }

    public static void e(Throwable tr) {
        if (DEBUG) {
            Log.e(PREFIX + "throw exception ", "---》 exception", tr);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(PREFIX + tag, msg);
        }
    }


}
