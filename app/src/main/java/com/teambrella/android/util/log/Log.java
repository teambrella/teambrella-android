package com.teambrella.android.util.log;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.crashlytics.android.Crashlytics;
import com.teambrella.android.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Log utility
 */

public final class Log {

    private static volatile HandlerThread sLoggerThread;
    private static volatile LoggerHandler sLoggerHandler;


    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }

    }


    public static void v(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.toString() : ""));
        }

        if (sLoggerHandler != null) {
            sLoggerHandler.msg(msg + " " + (tr != null ? tr.toString() : ""));
        }
    }


    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }

    }


    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.toString() : ""));
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg + " " + (tr != null ? tr.toString() : ""));
        }
    }


    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }
    }


    public static void i(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.toString() : ""));
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg + " " + (tr != null ? tr.toString() : ""));
        }
    }


    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }


    }


    public static void w(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.toString() : ""));
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }
    }


    public static void w(String tag, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, tr);
        } else {
            Crashlytics.log(tr != null ? tr.toString() : "");
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(tr != null ? tr.toString() : "");
        }

    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, msg);
        } else {
            Crashlytics.log(msg);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, msg, tr);
        } else {
            Crashlytics.log(msg + " " + (tr != null ? tr.toString() : ""));
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(msg + " " + (tr != null ? tr.toString() : ""));
        }

    }

    public static void reportNonFatal(String tag, Throwable e) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, "" + e.toString(), e);
        } else {
            Crashlytics.logException(e);
        }

        if (sLoggerHandler != null && sLoggerThread != null && sLoggerThread.isAlive()) {
            sLoggerHandler.msg(e != null ? e.toString() : "");
        }
    }

    public static synchronized void startDebugging(Context context) {
        if (sLoggerHandler == null) {
            sLoggerThread = new HandlerThread("Debug Logging");
            sLoggerThread.start();
            sLoggerHandler = new LoggerHandler(sLoggerThread.getLooper());
            sLoggerHandler.start(context);
        }
    }

    public static synchronized String stopDebugging() {
        String logPath = null;
        if (sLoggerHandler != null) {
            logPath = sLoggerHandler.stop();
            sLoggerHandler = null;
            sLoggerThread = null;
        }
        return logPath;
    }

    private static class LoggerHandler extends Handler {


        private PrintStream mPrintStream;
        private File mLogFile;

        LoggerHandler(Looper looper) {
            super(looper);
        }

        void start(Context context) {
            post(() -> {
                if (mPrintStream == null) {
                    //noinspection EmptyCatchBlock
                    try {
                        mLogFile = new File(context.getDir("log", Context.MODE_PRIVATE), String.format(Locale.US, "teambrella%d.log"
                                , System.currentTimeMillis()));
                        mPrintStream = new PrintStream(new FileOutputStream(mLogFile));
                    } catch (Exception e) {

                    }
                }
            });
        }

        String stop() {
            final Object lock = new Object();
            post(() -> {
                //noinspection EmptyCatchBlock
                try {
                    if (mPrintStream != null) {
                        mPrintStream.close();
                        sLoggerThread.quitSafely();
                    }
                } catch (Exception e) {

                } finally {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            synchronized (lock) {
                try {
                    lock.wait(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }

            return mLogFile.getAbsolutePath();
        }

        void msg(String msg) {
            post(() -> {
                if (mPrintStream != null) {
                    mPrintStream.println(msg);
                }
            });
        }
    }
}
