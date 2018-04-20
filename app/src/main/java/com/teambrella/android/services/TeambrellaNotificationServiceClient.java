package com.teambrella.android.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.teambrella.android.services.push.INotificationMessage;

/**
 * A Service Client
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaNotificationServiceClient implements TeambrellaNotificationService.ITeambrellaNotificationServiceBinder.INotificationServiceListener {


    private enum State {
        INIT,
        CONNECTING,
        CONNECTED,
        DISCONNECTED
    }


    private final Context mContext;
    private State mState = State.INIT;
    private TeambrellaNotificationService.ITeambrellaNotificationServiceBinder mBinder;


    /**
     * Constructor
     *
     * @param context to use
     */
    public TeambrellaNotificationServiceClient(Context context) {
        mContext = context;
    }


    public void connect() {
        if (mState == State.INIT) {
            if (mContext.bindService(new Intent(mContext, TeambrellaNotificationService.class), mServiceConnection, Service.BIND_AUTO_CREATE)) {
                mState = State.CONNECTING;
            }
        }
    }

    public void disconnect() {
        if (mState == State.CONNECTED) {
            mContext.unbindService(mServiceConnection);
        } else if (mState == State.CONNECTING) {
            mState = State.DISCONNECTED;
        }
        if (mBinder != null) {
            mBinder.unregisterListener(this);
        }
    }

    @Override
    public boolean onPushMessage(INotificationMessage message) {
        return false;
    }

    protected void onServiceConnected() {
        mBinder.registerListener(this);
    }

    protected void onServiceDisconnected() {
        mBinder.unregisterListener(this);
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (mState == State.CONNECTING) {
                mState = State.CONNECTED;
                mBinder = (TeambrellaNotificationService.ITeambrellaNotificationServiceBinder) iBinder;
                TeambrellaNotificationServiceClient.this.onServiceConnected();
            } else if (mState == State.DISCONNECTED) {
                mContext.unbindService(mServiceConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mState = State.DISCONNECTED;
            TeambrellaNotificationServiceClient.this.onServiceDisconnected();
            mBinder = null;
        }
    };


}
