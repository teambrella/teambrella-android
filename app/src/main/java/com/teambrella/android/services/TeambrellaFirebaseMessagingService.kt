package com.teambrella.android.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.R
import com.teambrella.android.TeambrellaApplication
import com.teambrella.android.backup.WalletBackUpService
import com.teambrella.android.services.push.*
import com.teambrella.android.ui.WelcomeActivity
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.TeambrellaUtilService
import com.teambrella.android.util.log.Log
import com.teambrella.android.wallet.TeambrellaWallet
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers

/**
 * Teambrella FireBase Messaging Service
 */
class TeambrellaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val LOG_TAG = "TeambrellaFirebaseMessagingService"
    }


    private var notificationManager: TeambrellaNotificationManager? = null
    private val handler = Handler();

    override fun onCreate() {
        super.onCreate()
        notificationManager = TeambrellaNotificationManager(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        Log.e(LOG_TAG, remoteMessage?.data?.toString() ?: "null")


        remoteMessage?.notification?.let {
            if (it.body != null) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                @Suppress("DEPRECATION")
                val builder = NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_teambrella_status)
                        .setColor(resources.getColor(R.color.lightBlue))
                        .setContentTitle(it.title ?: getString(R.string.app_name))
                        .setContentText(it.body)
                        .setStyle(NotificationCompat.BigTextStyle()
                                .bigText(it.body))
                        .setContentIntent(PendingIntent.getActivity(this
                                , 1
                                , Intent(this, WelcomeActivity::class.java)
                                , PendingIntent.FLAG_UPDATE_CURRENT))
                notificationManager.notify(it.body?.hashCode() ?: 999, builder.build())
            }
        }

        remoteMessage?.data?.let { _data ->
            val pushMessage = FireBaseNotificationMessage(_data)
            when (pushMessage.cmd) {
                CREATED_POST,
                DELETED_POST,
                TYPING,
                NEW_CLAIM,
                PRIVATE_MSG,
                WALLET_FUNDED,
                POSTS_SINCE_INTERACTED,
                NEW_TEAMMATE,
                NEW_DISCUSSION,
                TOPIC_MESSAGE_NOTIFICATION,
                PROXY,
                PROXY_SEED -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onPushMessage(this, pushMessage)
                    } else {
                        handler.post {
                            notificationManager?.handlePushMessage(pushMessage)
                        }
                    }
                }

                SYNC -> {
                    val context = this
                    Observable.create(ObservableOnSubscribe<Void> { emitter ->
                        TeambrellaWallet(context).syncWallet(TeambrellaWallet.SYNC_USER_PRESENT)
                        emitter.onComplete()
                    }).subscribeOn(Schedulers.io()).subscribe()
                }

                DEBUG_DB -> {
                    TeambrellaUtilService.scheduleDebugDB(this)
                    Log.reportNonFatal(LOG_TAG, DebugDBMessagingException())
                }
                DEBUG_SYNC -> {
                    TeambrellaUtilService.oneoffWalletSync(this, pushMessage.debug, true)
                    Log.reportNonFatal(LOG_TAG, DebugSyncMessagingException())
                }
                DEBUG_UPDATE -> {
                    TeambrellaUtilService.oneOffUpdate(this, pushMessage.debug)
                    Log.reportNonFatal(LOG_TAG, DebugUpdateMessagingException())
                }
                else -> {

                }
            }

            StatisticHelper.onPushMessage(this, pushMessage.cmd.toString(), false)

        }
        WalletBackUpService.schedulePeriodicBackupCheck(this)
    }

    private val isForeground: Boolean
        get() = (applicationContext as TeambrellaApplication).isForeground
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.O


    open class TeambrellaFirebaseMessagingException(message: String) : Exception(message)
    class DebugDBMessagingException : TeambrellaFirebaseMessagingException(DEBUG_DB.toString())
    class DebugSyncMessagingException : TeambrellaFirebaseMessagingException(DEBUG_SYNC.toString())
    class DebugUpdateMessagingException : TeambrellaFirebaseMessagingException(DEBUG_UPDATE.toString())

}