package com.teambrella.android.wallet

import android.os.Bundle
import android.support.v4.app.Fragment
import com.teambrella.android.ui.TeambrellaUser
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers


class TeambrellaWalletRequestFragment : Fragment() {


    companion object {

        fun createInstance(): TeambrellaWalletRequestFragment = TeambrellaWalletRequestFragment().apply {
            retainInstance = true
        }

        private const val MIN_SYNC_DELAY = (1000 * 60 * 5).toLong()
    }

    private lateinit var wallet: TeambrellaWallet
    private lateinit var user: TeambrellaUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = TeambrellaWallet(context)
        user = TeambrellaUser.get(context)
    }


    fun sync() {
        Observable.create(ObservableOnSubscribe<Unit> {
            if (canSyncByTime(System.currentTimeMillis())) {
                wallet.syncWallet(TeambrellaWallet.SYNC_UI)
            }
            it.onComplete()
        }).subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun update() {
        Observable.create(ObservableOnSubscribe<Unit> {
            wallet.updateWallet()
            it.onComplete()
        }).subscribeOn(Schedulers.io())
                .subscribe()
    }


    private fun canSyncByTime(time: Long): Boolean {
        val delay = time - user.lastSyncTime
        return Math.abs(delay) > MIN_SYNC_DELAY
    }
}