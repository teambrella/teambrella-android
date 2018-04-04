package com.teambrella.android.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.R
import com.teambrella.android.TeambrellaApplication
import com.teambrella.android.api.TeambrellaModel
import com.teambrella.android.backup.WalletBackUpService
import com.teambrella.android.ui.WelcomeActivity
import com.teambrella.android.ui.chat.ChatActivity
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


        private const val USER_ID = TeambrellaModel.ATTR_DATA_USER_ID
        private const val TEAM_ID = TeambrellaModel.ATTR_DATA_TEAM_ID
        private const val TOPIC_ID = TeambrellaModel.ATTR_DATA_TOPIC_ID
        private const val POST_ID = "PostId"
        private const val USER_NAME = "UserName"
        private const val USER_IMAGE = TeambrellaModel.ATTR_DATA_AVATAR
        private const val CONTENT = "Content"
        private const val AMOUNT = TeambrellaModel.ATTR_DATA_AMOUNT
        private const val TEAM_LOGO = TeambrellaModel.ATTR_DATA_TEAM_LOGO
        private const val TEAM_NAME = TeambrellaModel.ATTR_DATA_TEAM_NAME
        private const val COUNT = TeambrellaModel.ATTR_DATA_COUNT
        private const val BALANCE_CRYPTO = "BalanceCrypto"
        private const val BALANCE_FIAT = "BalanceFiat"
        private const val MESSAGE = TeambrellaModel.ATTR_REQUEST_MESSAGE
        private const val TEAMMATE_USER_ID = "TeammateUserId"
        private const val TEAMMATE_USER_NAME = "TeammateUserName"
        private const val TEAMMATE_AVATAR = "TeammateAvatar"
        private const val CLAIM_ID = "ClaimId"
        private const val CLAIM_USER_NAME = "ClaimUserName"
        private const val OBJECT_NAME = TeambrellaModel.ATTR_DATA_OBJECT_NAME
        private const val CLAIM_PHOTO = TeambrellaModel.ATTR_DATA_SMALL_PHOTO
        private const val TOPIC_NAME = "TopicName"
        private const val MY_TOPIC = "MyTopic"
    }


    var notificationManager: TeambrellaNotificationManager? = null


    override fun onCreate() {
        super.onCreate()
        notificationManager = TeambrellaNotificationManager(this)
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


        remoteMessage?.data?.let { data ->
            when (data[CMD]) {
                CREATED_POST -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onPostCreated(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[TOPIC_ID]
                                , data[POST_ID]
                                , data[USER_NAME]
                                , data[USER_IMAGE]
                                , data[CONTENT])
                    } else {
                        // nothing to do
                    }
                }

                DELETED_POST -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onPostDeleted(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[TOPIC_ID]
                                , data[POST_ID])
                    } else {
                        //nothing to do
                    }
                }

                TYPING -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onTyping(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[TOPIC_ID]
                                , data[POST_ID])
                    } else {
                        //nothing to do
                    }
                }

                NEW_CLAIM -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onNewClaim(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[CLAIM_ID]?.toInt() ?: 0
                                , data[USER_NAME]
                                , data[USER_IMAGE]
                                , data[AMOUNT]
                                , data[TEAM_LOGO]
                                , data[TEAM_NAME]);
                    } else {
                        notificationManager?.showNewClaimNotification(
                                data[TEAM_ID]?.toInt() ?: 0
                                , data[CLAIM_ID]?.toInt() ?: 0
                                , data[USER_NAME]
                                , data[AMOUNT]
                                , data[TEAM_NAME])
                    }
                }

                PRIVATE_MSG -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onPrivateMessage(this
                                , data[USER_ID]
                                , data[USER_NAME]
                                , data[USER_IMAGE]
                                , data[MESSAGE])
                    } else {
                        notificationManager?.showPrivateMessageNotification(data[USER_ID]
                                , data[USER_NAME]
                                , data[USER_IMAGE]
                                , data[MESSAGE]
                        )
                    }
                }

                WALLET_FUNDED -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onWalletFunded(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[BALANCE_CRYPTO]
                                , data[BALANCE_FIAT]
                                , data[TEAM_LOGO]
                                , data[TEAM_NAME])
                    } else {
                        notificationManager?.showWalletIsFundedNotification(data[AMOUNT])
                    }
                }

                POSTS_SINCE_INTERACTED -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onPostSinceInteracted(this,
                                data[COUNT]?.toInt() ?: 0)
                    } else {
                        notificationManager?.showNewMessagesSinceLastVisit(data[COUNT]?.toInt()
                                ?: 0)
                    }
                }

                NEW_TEAMMATE -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onNewTeammate(this
                                , data[USER_NAME]
                                , data[COUNT]?.toInt() ?: 0
                                , data[TEAM_NAME])
                    } else {
                        notificationManager?.showNewTeammates(data[USER_NAME]
                                , data[COUNT]?.toInt() ?: 0
                                , data[TEAM_NAME])
                    }
                }

                NEW_DISCUSSION -> {
                    if (isForeground) {
                        TeambrellaNotificationService.onNewDiscussion(this
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[USER_ID]
                                , data[TOPIC_ID]
                                , data[TOPIC_NAME]
                                , data[POST_ID]
                                , data[USER_NAME]
                                , data[USER_IMAGE]
                                , data[TEAM_LOGO]
                                , data[TEAM_NAME])
                    } else {
                        notificationManager?.showNewDiscussion(data[TEAM_NAME]
                                , data[USER_NAME]
                                , data[TEAM_ID]?.toInt() ?: 0
                                , data[TOPIC_NAME]
                                , data[TOPIC_ID])
                    }
                }

                TOPIC_MESSAGE_NOTIFICATION -> {
                    if (data[TEAMMATE_USER_ID] != null) {
                        if (isForeground) {
                            TeambrellaNotificationService.onNewApplicationChatMessage(this
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , data[TEAMMATE_USER_ID]
                                    , data[TOPIC_ID]
                                    , data[TEAMMATE_USER_NAME]
                                    , data[USER_ID]
                                    , data[USER_NAME]
                                    , data[USER_IMAGE]
                                    , data[CONTENT]
                                    , data[MY_TOPIC]?.toBoolean() ?: false)
                        } else {
                            notificationManager?.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.APPLICATION
                                    , data[TEAMMATE_USER_NAME]
                                    , data[USER_NAME]
                                    , data[CONTENT]
                                    , data[MY_TOPIC]?.toBoolean() ?: false
                                    , data[TOPIC_ID]
                                    , ChatActivity.getTeammateChat(this
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , data[TEAMMATE_USER_ID]
                                    , data[TEAMMATE_USER_NAME]
                                    , data[TEAMMATE_AVATAR]
                                    , data[TOPIC_ID]
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS))
                        }
                    } else if (data[CLAIM_ID] != null) {
                        if (isForeground) {
                            TeambrellaNotificationService.onNewClaimChatMessage(this
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , data[CLAIM_ID]?.toInt() ?: 0
                                    , data[CLAIM_USER_NAME]
                                    , data[USER_ID]
                                    , data[USER_NAME]
                                    , data[TOPIC_ID]
                                    , data[CONTENT]
                                    , data[OBJECT_NAME]
                                    , data[CLAIM_PHOTO]
                                    , data[MY_TOPIC]?.toBoolean() ?: false)
                        } else {
                            notificationManager?.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.CLAIM
                                    , data[CLAIM_USER_NAME]
                                    , data[USER_NAME]
                                    , data[CONTENT]
                                    , data[MY_TOPIC]?.toBoolean() ?: false
                                    , data[TOPIC_ID]
                                    , ChatActivity.getClaimChat(this
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , data[CLAIM_ID]?.toInt() ?: 0
                                    , data[OBJECT_NAME]
                                    , data[CLAIM_PHOTO]
                                    , data[TOPIC_ID]
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS
                                    , null));
                        }
                    } else if (data[TOPIC_NAME] != null) {
                        if (isForeground) {
                            TeambrellaNotificationService.onNewDiscussionChatMessage(this
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , data[USER_ID]
                                    , data[USER_NAME]
                                    , data[TOPIC_NAME]
                                    , data[TOPIC_ID]
                                    , data[CONTENT])
                        } else {
                            notificationManager?.showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.DISCUSSION
                                    , data[TOPIC_NAME]
                                    , data[USER_ID]
                                    , data[CONTENT]
                                    , data[MY_TOPIC]?.toBoolean() ?: false
                                    , data[TOPIC_ID]
                                    , ChatActivity.getFeedChat(this
                                    , data[TOPIC_NAME]
                                    , data[TOPIC_ID]
                                    , data[TEAM_ID]?.toInt() ?: 0
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS));
                        }
                    } else {

                    }
                }


                DEBUG_DB -> {
                    TeambrellaUtilService.scheduleDebugDB(this)
                    Log.reportNonFatal(LOG_TAG, DebugDBMessagingException())
                }
                DEBUG_SYNC -> {
                    TeambrellaUtilService.oneoffWalletSync(this, (data[DEBUG]
                            ?: "false").toBoolean(), true)
                    Log.reportNonFatal(LOG_TAG, DebugSyncMessagingException())
                }
                DEBUG_UPDATE -> {
                    TeambrellaUtilService.oneOffUpdate(this, (data[DEBUG]
                            ?: "false").toBoolean())
                    Log.reportNonFatal(LOG_TAG, DebugUpdateMessagingException())
                }

                else -> {

                }
            }
        }
        WalletBackUpService.schedulePeriodicBackupCheck(this)
    }


    private val isForeground: Boolean = (applicationContext as TeambrellaApplication).isForeground
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O


    open class TeambrellaFirebaseMessagingException(message: String) : Exception(message)
    class DebugDBMessagingException : TeambrellaFirebaseMessagingException(DEBUG_DB)
    class DebugSyncMessagingException : TeambrellaFirebaseMessagingException(DEBUG_SYNC)
    class DebugUpdateMessagingException : TeambrellaFirebaseMessagingException(DEBUG_UPDATE)

}