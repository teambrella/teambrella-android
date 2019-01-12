package com.teambrella.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.teambrella.android.services.push.BundleNotificationMessage;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service {


    private static final String ACTION_ON_PUSH_MESSAGE = "onPushMessageAction";


    public interface ITeambrellaNotificationServiceBinder extends IBinder {

        interface INotificationServiceListener {
            boolean onPushMessage(INotificationMessage message);
        }

        void registerListener(INotificationServiceListener listener);

        void unregisterListener(INotificationServiceListener listener);
    }


    public static final String LOG_TAG = TeambrellaNotificationService.class.getSimpleName();

    public static final String CONNECT_ACTION = "connect";

    private CopyOnWriteArrayList<ITeambrellaNotificationServiceBinder.INotificationServiceListener> mListeners = new CopyOnWriteArrayList<>();
    private TeambrellaNotificationSocketClient mTeambrellaSocketClient;
    private TeambrellaNotificationManager mTeambrellaNotificationManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TeambrellaNotificationServiceBinder();
    }


    private class TeambrellaNotificationServiceBinder extends Binder implements ITeambrellaNotificationServiceBinder {

        @Override
        public void registerListener(INotificationServiceListener listener) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        @Override
        public void unregisterListener(INotificationServiceListener listener) {
            mListeners.remove(listener);
        }
    }

    public static void onPushMessage(Context context, INotificationMessage message) {
        try {
            context.startService(new Intent(context, TeambrellaNotificationService.class)
                    .setAction(ACTION_ON_PUSH_MESSAGE)
                    .putExtras(new BundleNotificationMessage(message).getData())
            );
        } catch (Exception e) {
            Log.reportNonFatal(LOG_TAG, e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mTeambrellaSocketClient != null) {
            mTeambrellaSocketClient.close();
        }
        super.onDestroy();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case CONNECT_ACTION:
                    TeambrellaUser user = TeambrellaUser.get(this);

                    if (mTeambrellaSocketClient != null) {
                        if (mTeambrellaSocketClient.isClosed()
                                || mTeambrellaSocketClient.isClosing()) {
                            mTeambrellaSocketClient.close();
                            mTeambrellaSocketClient = null;
                        }
                    }

                    if (mTeambrellaSocketClient == null && user != null && !user.isDemoUser() && user.getPrivateKey() != null) {
                        mTeambrellaSocketClient = new TeambrellaNotificationSocketClient(this);
                    }
                    return START_STICKY;

                case Intent.ACTION_BOOT_COMPLETED:
                    Log.e(LOG_TAG, "boot complete");
                    return START_STICKY;

                case ACTION_ON_PUSH_MESSAGE: {
                    INotificationMessage message = new BundleNotificationMessage(intent.getExtras());
                    if (!handlePushMessage(message)) {
                        TeambrellaNotificationManager notificationManager = getTeambrellaNotificationManager();
                        if (notificationManager != null) {
                            notificationManager.handlePushMessage(message);
                        }
                    }
                }

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    private boolean handlePushMessage(INotificationMessage message) {
        boolean result = false;
        for (ITeambrellaNotificationServiceBinder.INotificationServiceListener listener : mListeners) {
            result |= listener.onPushMessage(message);
        }
        return result;
    }


    private TeambrellaNotificationManager getTeambrellaNotificationManager() {
        TeambrellaUser user = TeambrellaUser.get(this);
        if (mTeambrellaNotificationManager == null && !user.isDemoUser()) {
            mTeambrellaNotificationManager = new TeambrellaNotificationManager(this);
        }
        return mTeambrellaNotificationManager;
    }

}
