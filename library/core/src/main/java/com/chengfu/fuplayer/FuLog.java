package com.chengfu.fuplayer;

import android.util.Log;

public class FuLog {

    public static boolean enabled = true;

    private FuLog() {
    }

    public static int v(String tag, String msg) {
        if (enabled) {
            return Log.v(tag, msg);
        }
        return 0;
    }


    public static int v(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.v(tag, msg, tr);
        }
        return 0;
    }


    public static int d(String tag, String msg) {
        if (enabled) {
            return Log.d(tag, msg);
        }
        return 0;
    }


    public static int d(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.d(tag, msg, tr);
        }
        return 0;
    }


    public static int i(String tag, String msg) {
        if (enabled) {
            return Log.i(tag, msg);
        }
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.i(tag, msg, tr);
        }
        return 0;
    }

    public static int w(String tag, String msg) {
        if (enabled) {
            return Log.w(tag, msg);
        }
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.w(tag, msg, tr);
        }
        return 0;
    }

    public static int w(String tag, Throwable tr) {
        if (enabled) {
            return Log.w(tag, tr);
        }
        return 0;
    }

    public static int e(String tag, String msg) {
        if (enabled) {
            return Log.e(tag, msg);
        }
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.e(tag, msg, tr);
        }
        return 0;
    }
}
