package com.teambrella.android.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

import java.net.URI;


/**
 * Teambrella Notification Service
 */
public class TeambrellaNotificationService extends Service implements TeambrellaServer.SocketClientListener {


    public static final String LOG_TAG = TeambrellaNotificationService.class.getSimpleName();

    public static final String CONNECT_ACTION = "connect";
    public static final String STOP_ACTION = "stop";
    public static final String EXTRA_TEAM_ID = "teamID";

    public static final String ON_NEW_MESSAGE_RECEIVED = "OnNewMessageReceived";


    private TeambrellaServer.TeambrellaSocketClient mTeambrellaSocketClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;


        if (action != null) {
            switch (action) {
                case CONNECT_ACTION:
                    URI uri = URI.create(new Uri.Builder()
                            .scheme("wss")
                            .authority("192.168.0.222")
                            .appendEncodedPath("wshandler.ashx")
                            .build().toString());
                    mTeambrellaSocketClient = new TeambrellaServer(this, TeambrellaUser.get(this).getPrivateKey())
                            .createSocketClient(uri, intent.getIntExtra(EXTRA_TEAM_ID, 0), this);
                    mTeambrellaSocketClient.connect();
                    return START_STICKY;
                case STOP_ACTION:
                    mTeambrellaSocketClient.close();
                    stopSelf();
                    return START_NOT_STICKY;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onMessage(String message) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ON_NEW_MESSAGE_RECEIVED));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e(LOG_TAG, "on close " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(LOG_TAG, "on error");
    }
}
