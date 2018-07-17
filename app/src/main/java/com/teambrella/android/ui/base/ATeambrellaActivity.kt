package com.teambrella.android.ui.base

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.google.gson.JsonObject
import com.teambrella.android.BuildConfig
import com.teambrella.android.api.TeambrellaModel
import com.teambrella.android.api.TeambrellaServerException
import com.teambrella.android.api.recommendedVersion
import com.teambrella.android.api.status
import com.teambrella.android.dagger.Dependencies
import com.teambrella.android.image.TeambrellaImageLoader
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.app.AppOutdatedActivity
import com.teambrella.android.ui.demo.NewDemoSessionActivity
import com.teambrella.android.util.log.Log
import com.teambrella.android.wallet.TeambrellaWalletRequestFragment
import io.reactivex.Notification
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject
import javax.inject.Named

abstract class ATeambrellaActivity : ATeambrellaDataHostActivity() {

    companion object {
        const val EXTRA_BACK_PRESSED_INTENT = "extra_back_pressed_intent"
        private val LOG_TAG = ATeambrellaActivity::class.java.simpleName
        private const val WALLET_DATA_FRAGMENT_TAG = "wallet_data_fragment"
        private const val MIN_RECOMMENDED_VERSION_DELAY = (1000 * 60 * 60 * 24 * 3).toLong()
    }

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_USER)
    lateinit var user: TeambrellaUser

    @Inject
    @field:Named(Dependencies.IMAGE_LOADER)
    lateinit var mImageLoader: TeambrellaImageLoader


    private val mLifecycleCallbacks = LinkedList<TeambrellaActivityLifecycle>()
    private val serverErrorHandler = ServerErrorHandler()


    fun registerLifecycleCallback(lifecycle: TeambrellaActivityLifecycle) {
        mLifecycleCallbacks.add(lifecycle)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onCreate(this, savedInstanceState)
        }

        val fragmentManager = supportFragmentManager

        if (fragmentManager.findFragmentByTag(WALLET_DATA_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction().add(TeambrellaWalletRequestFragment(), WALLET_DATA_FRAGMENT_TAG).commit()
        }
    }

    override fun onStart() {
        super.onStart()
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onStart()
        }

        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(WALLET_DATA_FRAGMENT_TAG) as TeambrellaWalletRequestFragment
        fragment.sync()

        serverErrorHandler.onStart()

    }

    override fun onResume() {
        super.onResume()
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onResume()
        }
    }

    protected fun getImageLoader(): TeambrellaImageLoader? {
        return mImageLoader
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val backPressedIntent = intent?.getParcelableExtra<PendingIntent>(EXTRA_BACK_PRESSED_INTENT)
        if (backPressedIntent != null) {
            try {
                backPressedIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                Log.e(LOG_TAG, e.toString())
            }

        }
    }

    override fun onPause() {
        super.onPause()
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onStop()
        }
        serverErrorHandler.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        for (lifecycle in mLifecycleCallbacks) {
            lifecycle.onDestroy(this)
        }
    }

    private inner class ServerErrorHandler {
        private val mCheckErrorDisposables = LinkedList<Disposable>()

        fun onStart() {
//            if (dataTags.isNotEmpty()) {
//                for (tag in dataTags) {
//                    mCheckErrorDisposables.add(getObservable(tag).subscribe(this::checkServerError))
//                }
//            }
//
//            if (dataPagerTags.isNotEmpty()) {
//                for (tag in dataPagerTags) {
//                    mCheckErrorDisposables.add(getPager(tag).dataObservable.subscribe(this::checkServerError))
//                }
//            }

        }

        fun onStop() {
//            mCheckErrorDisposables.iterator().let {
//                while (it.hasNext()) {
//                    it.next().takeIf { !it.isDisposed }?.dispose()
//                    it.remove()
//                }
//            }
        }

        private fun checkServerError(notification: Notification<JsonObject>) {
            if (notification.isOnError) {
                val error = notification.error
                if (error is TeambrellaServerException) {
                    when (error.errorCode) {
                        TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH -> {
                            if (!isFinishing && user.isDemoUser) {
                                startActivity(Intent(this@ATeambrellaActivity, NewDemoSessionActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                finish()
                            }
                            if (!isFinishing) {
                                AppOutdatedActivity.start(this@ATeambrellaActivity, true)
                                finish()
                            }
                        }
                        TeambrellaModel.VALUE_STATUS_RESULT_NOT_SUPPORTED_CLIENT_VERSION -> if (!isFinishing) {
                            AppOutdatedActivity.start(this@ATeambrellaActivity, true)
                            finish()
                        }
                    }
                }
            } else {
                notification.value.status?.let { _status ->
                    val recommendedVersion = _status.recommendedVersion
                    if (recommendedVersion > BuildConfig.VERSION_CODE) {
                        val current = System.currentTimeMillis()
                        if (Math.abs(current - user.newVersionLastScreenTime) < MIN_RECOMMENDED_VERSION_DELAY) {
                            return
                        }
                        AppOutdatedActivity.start(this@ATeambrellaActivity, false)
                        user.newVersionLastScreenTime = current
                    }
                }
            }
        }
    }


}