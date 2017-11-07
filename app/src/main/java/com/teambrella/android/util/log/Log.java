package com.teambrella.android.util.log;

import com.crashlytics.android.Crashlytics;
import com.teambrella.android.BuildConfig;

/**
 * Log utility
 */

public final class Log {

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(tag, msg);
        } else {
            Crashlytics.log(msg);
        }
    }


    public static void v(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.getMessage() : ""));
        }
    }


    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

    }


    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.getMessage() : ""));
        }
    }


    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, msg);
        } else {
            Crashlytics.log(msg);
        }
    }


    public static void i(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.getMessage() : ""));
        }
    }


    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, msg);
        } else {
            Crashlytics.log(msg);
        }
    }


    public static void w(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.getMessage() : ""));
        }
    }


    public static void w(String tag, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, tr);
        } else {
            Crashlytics.log(tr != null ? tr.getMessage() : "");
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, msg);
        } else {
            Crashlytics.log(msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.getMessage() : ""));
        }
    }

    public static void reportNonFatal(String tag, Exception e) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, "" + e.getMessage(), e);
        } else {
            Crashlytics.logException(e);
        }
    }

    public static void reportNonFatal(String tag, String msg) {
        reportNonFatal(tag, new Exception(msg));
    }
}
