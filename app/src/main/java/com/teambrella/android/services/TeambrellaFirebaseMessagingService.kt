package com.teambrella.android.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.R
import com.teambrella.android.TeambrellaApplication
import com.teambrella.android.backup.WalletBackUpService
import com.teambrella.android.ui.WelcomeActivity
import com.teambrella.android.util.TeambrellaUtilService
import com.teambrella.android.util.log.Log

/**
 * Teambrella FireBase Messaging Service
 */
class TeambrellaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {

        private const val LOG_TAG = "TeambrellaFirebaseMessagingService"

        private const val CREATED_POST = "1"
        private const val DELETED_POST = "2"
        private const val TYPING = "13"
        private const val NEW_CLAIM = "4"
        private const val PRIVATE_MSG = "5"
        private const val WALLET_FUNDED = "6"
        private const val POSTS_SINCE_INTERACTED = "7"
        private const val NEW_TEAMMATE = "8"
        private const val NEW_DISCUSSION = "9"
        private const val TOPIC_MESSAGE_NOTIFICATION = "21"
        private const val DEBUG_DB = "101"
        private const val DEBUG_UPDATE = "102"
        private const val DEBUG_SYNC = "103"
        private const val CMD = "Cmd"
        private const val DEBUG = "Debug"


    }


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        remoteMessage?.notification?.let {
            if (it.body != null) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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


        remoteMessage?.data?.let {
            when (it[CMD]) {

                CREATED_POST -> {

                }

                DELETED_POST -> {

                }

                TYPING -> {

                }

                NEW_CLAIM -> {

                }

                PRIVATE_MSG -> {

                }

                WALLET_FUNDED -> {

                }

                POSTS_SINCE_INTERACTED -> {

                }

                NEW_TEAMMATE -> {

                }

                NEW_DISCUSSION -> {

                }

                TOPIC_MESSAGE_NOTIFICATION -> {

                }


                DEBUG_DB -> {
                    TeambrellaUtilService.scheduleDebugDB(this)
                    Log.reportNonFatal(LOG_TAG, DebugDBMessagingException())
                }
                DEBUG_SYNC -> {
                    TeambrellaUtilService.oneoffWalletSync(this, (it[DEBUG]
                            ?: "false").toBoolean(), true)
                    Log.reportNonFatal(LOG_TAG, DebugSyncMessagingException())
                }
                DEBUG_UPDATE -> {
                    TeambrellaUtilService.oneOffUpdate(this, (it[DEBUG]
                            ?: "false").toBoolean())
                    Log.reportNonFatal(LOG_TAG, DebugUpdateMessagingException())
                }
            }
        }
        WalletBackUpService.schedulePeriodicBackupCheck(this)
    }


    private fun isForeground(): Boolean = (applicationContext as TeambrellaApplication).isForeground


    open class TeambrellaFirebaseMessagingException(message: String) : Exception(message)
    class DebugDBMessagingException : TeambrellaFirebaseMessagingException(DEBUG_DB)
    class DebugSyncMessagingException : TeambrellaFirebaseMessagingException(DEBUG_SYNC)
    class DebugUpdateMessagingException : TeambrellaFirebaseMessagingException(DEBUG_UPDATE)

}