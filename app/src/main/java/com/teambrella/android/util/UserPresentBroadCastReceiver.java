package com.teambrella.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.wallet.TeambrellaWallet;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;


/**
 * User Present Broadcast Receiver
 */
public class UserPresentBroadCastReceiver extends BroadcastReceiver {
    private static final long MIN_SYNC_DELAY = 1000 * 60 * 30;

    @Override
    public void onReceive(final Context context, Intent intent) {
        TeambrellaUser user = TeambrellaUser.get(context);
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_USER_PRESENT.equals(action) && !user.isDemoUser() && user.getPrivateKey() != null) {
            long delay = System.currentTimeMillis() - TeambrellaUser.get(context).getLastSyncTime();
            if (Math.abs(delay) > MIN_SYNC_DELAY) {
                Observable.create((ObservableOnSubscribe<Void>) emitter -> {
                    new TeambrellaWallet(context).syncWallet(TeambrellaWallet.SYNC_USER_PRESENT);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io()).subscribe();
            }
        }
    }
}
