package com.teambrella.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.teambrella.android.util.log.Log
import com.teambrella.android.wallet.TeambrellaWallet
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers

class PackageReplacedBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Observable.create(ObservableOnSubscribe<Void> { emitter ->
                TeambrellaWallet(context).syncWallet(TeambrellaWallet.SYNC_APP_UPDATED)
                emitter.onComplete()
            }).subscribeOn(Schedulers.io()).subscribe()
            StatisticHelper.onApplicationUpdated(context)
        }
    }
}