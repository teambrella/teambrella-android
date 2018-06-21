package com.teambrella.android.wallet

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.background.BackgroundRestrictionsActivity
import com.teambrella.android.util.isHuaweiProtectedAppAvailable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs
import kotlin.math.max


class TeambrellaWalletRequestFragment : Fragment() {


    companion object {

        fun createInstance(): TeambrellaWalletRequestFragment = TeambrellaWalletRequestFragment().apply {
            retainInstance = true
        }

        private const val MIN_SYNC_DELAY = (1000 * 60 * 5).toLong()
        private const val MIN_SYNC_DELAY_WARNING = (1000 * 60 * 60 * 24 * 3).toLong()
    }

    private lateinit var wallet: TeambrellaWallet
    private lateinit var user: TeambrellaUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = TeambrellaWallet(context)
        user = TeambrellaUser.get(context)
    }


    fun sync() {

        if (!user.isDemoUser) {
            if (abs(max(user.lastSyncTime, user.lastBackgroundRestrictionScreenTime) - System.currentTimeMillis()) > MIN_SYNC_DELAY_WARNING) {
                if (context?.isHuaweiProtectedAppAvailable == true) {
                    startActivity(Intent(context, BackgroundRestrictionsActivity::class.java))
                }
                user.lastBackgroundRestrictionScreenTime = System.currentTimeMillis()
            }
        }

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