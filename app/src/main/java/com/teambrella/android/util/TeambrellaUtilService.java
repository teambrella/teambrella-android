package com.teambrella.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.iid.FirebaseInstanceId;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.backup.WalletBackUpService;
import com.teambrella.android.services.TeambrellaNotificationManager;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;
import com.teambrella.android.wallet.TeambrellaWallet;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED;


/**
 * Teambrella util service
 */
public class TeambrellaUtilService extends GcmTaskService {

    private static final long MIN_SYNC_DELAY = 1000 * 60 * 30;
    private static final String ACTION_LOCAL_SYNC = "com.teambrella.android.util.ACTION_SYNC";
    private static final String EXTRA_TAG = "extra_tag";
    private static final String TASK_EXTRAS = "tag_extras";

    public static final String SYNC_WALLET_TASK_TAG = "TEAMBRELLA-SYNC-WALLET";
    public static final String SYNC_WALLET_ONCE_TAG = "TEAMBRELLA-SYNC-WALLET-ONCE";
    public static final String CHECK_SOCKET = "TEAMBRELLA_CHECK_SOCKET";
    public static final String DEBUG_DB_TASK_TAG = "TEAMBRELLA_DEBUG_DB";
    public static final String DEBUG_UPDATE_TAG = "TEAMBRELLA_DEBUG_UPDATE";

    private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();
    private static final String EXTRA_DEBUG_LOGGING = "debug_logging";
    private static final String EXTRA_FORCE = "force";


    private TeambrellaWallet mTeambrellaWallet;

    public static void scheduleWalletSync(Context context) {
        if (checkGooglePlayServices(context)) {
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(TeambrellaUtilService.class)
                    .setTag(TeambrellaUtilService.SYNC_WALLET_TASK_TAG)
                    .setUpdateCurrent(true) // kill tasks with the same tag if any
                    .setPersisted(true)
                    .setPeriod(30 * 60)     // 30 minutes period
                    .setRequiredNetwork(NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(task);
        }
    }

    public static void oneoffWalletSync(Context context) {
        oneoffWalletSync(context, false, false);
    }


    public static void oneoffWalletSync(Context context, boolean debug, boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkGooglePlayServices(context)) {
                Bundle args = new Bundle();
                args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
                args.putBoolean(EXTRA_FORCE, force);
                OneoffTask task = new OneoffTask.Builder()
                        .setService(TeambrellaUtilService.class)
                        .setTag(SYNC_WALLET_ONCE_TAG)
                        .setExecutionWindow(0, 1)
                        .setExtras(args)
                        .build();
                GcmNetworkManager.getInstance(context).schedule(task);
            }
        } else {
            try {
                Bundle args = new Bundle();
                args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
                args.putBoolean(EXTRA_FORCE, force);
                context.startService(new Intent(context, TeambrellaUtilService.class)
                        .setAction(ACTION_LOCAL_SYNC)
                        .putExtra(EXTRA_TAG, SYNC_WALLET_ONCE_TAG)
                        .putExtra(TASK_EXTRAS, args));
            } catch (Throwable ignored) {
            }
        }
    }

    public static void oneOffUpdate(Context context, boolean debug) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkGooglePlayServices(context)) {
                Bundle args = new Bundle();
                args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
                OneoffTask task = new OneoffTask.Builder()
                        .setService(TeambrellaUtilService.class)
                        .setTag(DEBUG_UPDATE_TAG)
                        .setExecutionWindow(0, 1)
                        .setExtras(args)
                        .build();
                GcmNetworkManager.getInstance(context).schedule(task);
            }
        } else {
            try {
                Bundle args = new Bundle();
                args.putBoolean(EXTRA_DEBUG_LOGGING, debug);
                context.startService(new Intent(context, TeambrellaUtilService.class)
                        .setAction(ACTION_LOCAL_SYNC)
                        .putExtra(EXTRA_TAG, DEBUG_UPDATE_TAG)
                        .putExtra(TASK_EXTRAS, args));
            } catch (Throwable ignored) {

            }
        }

    }

    public static void scheduleCheckingSocket(Context context) {
        if (checkGooglePlayServices(context)) {
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(TeambrellaUtilService.class)
                    .setTag(TeambrellaUtilService.CHECK_SOCKET)
                    .setUpdateCurrent(true) // kill tasks with the same tag if any
                    .setPersisted(true)
                    .setPeriod(60)     // 30 minutes period
                    .setFlex(30)       // +/- 10 minutes
                    .setUpdateCurrent(true) // kill tasks with the same tag if any
                    .setRequiredNetwork(NETWORK_STATE_CONNECTED)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(task);
        }
    }


    public static void scheduleDebugDB(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkGooglePlayServices(context)) {
                OneoffTask task = new OneoffTask.Builder()
                        .setService(TeambrellaUtilService.class)
                        .setTag(DEBUG_DB_TASK_TAG)
                        .setExecutionWindow(0, 1)
                        .build();
                GcmNetworkManager.getInstance(context).schedule(task);
            }
        } else {
            try {
                context.startService(new Intent(context, TeambrellaUtilService.class)
                        .setAction(ACTION_LOCAL_SYNC)
                        .putExtra(EXTRA_TAG, DEBUG_DB_TASK_TAG));
            } catch (Throwable ignored) {

            }
        }
    }

    public static boolean checkGooglePlayServices(Context context) {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        boolean result;
        switch (code) {
            case ConnectionResult.SUCCESS:
                result = true;
                break;
            case ConnectionResult.SERVICE_MISSING:
                Log.e(LOG_TAG, "google play service missing");
                result = false;
                break;
            case ConnectionResult.SERVICE_UPDATING:
                Log.e(LOG_TAG, "google play service updating");
                result = false;
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Log.e(LOG_TAG, "google play service update required");
                result = false;
                break;
            case ConnectionResult.SERVICE_DISABLED:
                Log.e(LOG_TAG, "google play service disabled");
                result = false;
                break;
            case ConnectionResult.SERVICE_INVALID:
                Log.e(LOG_TAG, "google play service invalid");
                result = false;
                break;
            default:
                result = false;
        }

        if (!result) {
            GoogleApiAvailability.getInstance().showErrorNotification(context, code);
            if (!BuildConfig.DEBUG) {
                Crashlytics.logException(new Exception("google play service error"));
            }
        }

        return result;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mTeambrellaWallet = new TeambrellaWallet(this);
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        scheduleWalletSync(this);
        scheduleCheckingSocket(this);
        WalletBackUpService.Companion.schedulePeriodicBackupCheck(this);

        checkHuaweiBackgroundRestriction(this);

        Observable.create((ObservableOnSubscribe<Void>) emitter -> {
            mTeambrellaWallet.syncWallet(TeambrellaWallet.SYNC_INITIALIZE);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).subscribe();

        StatisticHelper.onApplicationInitialize(this);
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_LOCAL_SYNC.equals(action)) {
            TaskParams taskParams = new TaskParams(intent.getStringExtra(EXTRA_TAG)
                    , intent.getBundleExtra(TASK_EXTRAS));
            new Thread(() -> onRunTask(taskParams)).start();
            return START_STICKY;
        } else {
            return super.onStartCommand(intent, i, i1);
        }
    }

    @Override
    public synchronized int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();
        if (tag != null) {
            switch (tag) {
                case SYNC_WALLET_TASK_TAG:
                case SYNC_WALLET_ONCE_TAG: {
                    if (isForce(taskParams) || canSyncByTime(System.currentTimeMillis())) {
                        if (isDebugLogging(taskParams)) {
                            Log.startDebugging(this);
                        }
                        Log.d(LOG_TAG, "---> SYNC -> onRunTask() started... tag:" + tag);
                        mTeambrellaWallet.syncWallet(TeambrellaWallet.SYNC_JOB);
                        if (isDebugLogging(taskParams)) {
                            String path = Log.stopDebugging();
                            if (path != null) {
                                debugLog(this, path);
                            }
                        }
                    }
                }
                break;

                case DEBUG_UPDATE_TAG: {
                    if (isDebugLogging(taskParams)) {
                        Log.startDebugging(this);
                    }
                    Log.d(LOG_TAG, "---> UPDATE -> onRunTask() started... tag:" + tag);
                    mTeambrellaWallet.updateWallet();

                    if (isDebugLogging(taskParams)) {
                        String path = Log.stopDebugging();
                        if (path != null) {
                            debugLog(this, path);
                        }
                    }
                }
                break;

                case CHECK_SOCKET:
                    //noinspection EmptyCatchBlock
                    try {
                        startService(new Intent(this, TeambrellaNotificationService.class)
                                .setAction(TeambrellaNotificationService.CONNECT_ACTION));
                    } catch (Exception e) {
                    }
                    break;
                case DEBUG_DB_TASK_TAG:
                    debugDB(this);
                    break;
            }
        } else {
            Log.reportNonFatal(LOG_TAG, new Exception("onRunTask got null tag."));
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }


    private static void checkHuaweiBackgroundRestriction(Context context) {
        final long minDelay = 1000 * 60 * 60 * 24 * 3;
        TeambrellaUser user = TeambrellaUser.get(context);
        if (!user.isDemoUser() && user.getPrivateKey() != null) {
            if (Math.abs(Math.max(user.getLastSyncTime(), user.getLastBackgroundRestrictionNotificationTime()) - System.currentTimeMillis()) > minDelay) {
                if (BackgroundUtils.isHuaweiProtectedAppAvailable(context)) {
                    new TeambrellaNotificationManager(context).showBackgroundRestrictionsNotification();
                    StatisticHelper.onBackgroundRestrictionNotification(context);
                }
                user.setLastBackgroundRestrictionNotificationTime(System.currentTimeMillis());
            }
        }
    }

    private static boolean isDebugLogging(TaskParams params) {
        Bundle extras = params.getExtras();
        return extras != null && extras.getBoolean(EXTRA_DEBUG_LOGGING, false);
    }

    private static boolean isForce(TaskParams params) {
        Bundle extras = params.getExtras();
        return extras != null && extras.getBoolean(EXTRA_FORCE, false);
    }


    @SuppressLint("CheckResult")
    private static void debugDB(Context context) {
        try {
            TeambrellaUser user = TeambrellaUser.get(context);
            TeambrellaServer server = new TeambrellaServer(context, user.getPrivateKey(), user.getDeviceCode(), !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null
                    , user.getInfoMask(context));
            server.requestObservable(TeambrellaUris.getDebugDbUri(context.getDatabasePath("teambrella").getAbsolutePath()), null)
                    .blockingFirst();
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
    }

    @SuppressLint("CheckResult")
    private static void debugLog(Context context, String logPath) {
        try {
            TeambrellaUser user = TeambrellaUser.get(context);
            TeambrellaServer server = new TeambrellaServer(context, user.getPrivateKey(), user.getDeviceCode(), !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null
                    , user.getInfoMask(context));
            server.requestObservable(TeambrellaUris.getDebugLogUri(logPath), null)
                    .blockingFirst();

            File file = new File(logPath);
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
    }


    private boolean canSyncByTime(long time) {
        long delay = time - TeambrellaUser.get(this).getLastSyncTime();
        Log.d(LOG_TAG, "" + (delay / (1000 * 60)) + " minutes since last sync");
        return Math.abs(delay) > MIN_SYNC_DELAY;
    }
}
