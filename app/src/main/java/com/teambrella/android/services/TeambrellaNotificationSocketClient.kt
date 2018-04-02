package com.teambrella.android.services

import android.content.Context
import android.net.Uri
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.util.TeambrellaUtilService
import java.lang.Exception
import java.net.URI

class TeambrellaNotificationSocketClient(val context: Context) : TeambrellaServer.SocketClientListener {


    companion object {
        private const val CREATED_POST = 1
        private const val DELETED_POST = 2
        private const val TYPING = 13
        private const val NEW_CLAIM = 4
        private const val PRIVATE_MSG = 5
        private const val WALLET_FUNDED = 6
        private const val POSTS_SINCE_INTERACTED = 7
        private const val NEW_TEAMMATE = 8
        private const val NEW_DISCUSSION = 9
        private const val TOPIC_MESSAGE_NOTIFICATION = 21
        private const val DEBUG_DB = 101
        private const val DEBUG_UPDATE = 102
        private const val DEBUG_SYNC = 103
    }


    private val socketClient: TeambrellaServer.TeambrellaSocketClient
    private val gson = GsonBuilder().setLenient().create()

    init {
        val uri = URI.create(Uri.Builder()
                .scheme("wss")
                .authority(TeambrellaServer.AUTHORITY)
                .appendEncodedPath("wshandler.ashx")
                .build().toString())

        val user = TeambrellaUser.get(context)

        val server = TeambrellaServer(context, user.privateKey
                , user.deviceCode
                , FirebaseInstanceId.getInstance().token
                , user.getInfoMask(context))
        socketClient = server.createSocketClient(uri, this, user.notificationTimeStamp)
    }


    override fun onOpen() {

    }

    override fun onMessage(message: String?) {

        val messageObject = message?.let {
            gson.fromJson(it, JsonObject::class.java)
        }

        messageObject?.let {
            when (it.cmd) {
                CREATED_POST -> {
                    TeambrellaNotificationService.onPostCreated(context
                            , it.teamId ?: 0
                            , it.userId
                            , it.topicId
                            , it.postId
                            , it.userName
                            , it.avatar
                            , it.content)
                }

                DELETED_POST -> {
                    TeambrellaNotificationService.onPostDeleted(context
                            , it.teamId ?: 0
                            , it.userId
                            , it.topicId
                            , it.postId)
                }

                TYPING -> {
                    TeambrellaNotificationService.onTyping(context
                            , it.teamId ?: 0
                            , it.userId
                            , it.topicId
                            , it.postId)
                }

                NEW_CLAIM -> {
                    TeambrellaNotificationService.onNewClaim(context
                            , it.teamId ?: 0
                            , it.userId
                            , it.claimId ?: 0
                            , it.userName
                            , it.avatar
                            , it.amountStr
                            , it.teamLogo
                            , it.teamName)

                }

                PRIVATE_MSG -> {
                    TeambrellaNotificationService.onPrivateMessage(context
                            , it.userId
                            , it.userName
                            , it.avatar
                            , it.message
                    )
                }

                WALLET_FUNDED -> {
                    TeambrellaNotificationService.onWalletFunded(context
                            , it.teamId ?: 0
                            , it.userId
                            , it.balanceCrypto
                            , it.balanceFiat
                            , it.teamLogo
                            , it.teamName)
                }

                POSTS_SINCE_INTERACTED -> {
                    TeambrellaNotificationService.onPostSinceInteracted(context,
                            it.count ?: 0)
                }

                NEW_TEAMMATE -> {
                    TeambrellaNotificationService.onNewTeammate( context,
                            it.userName
                            , it.count ?: 0
                            , it.teamName
                    )
                }

                NEW_DISCUSSION -> {
                    TeambrellaNotificationService.onNewDiscussion(context
                        , it.teamId ?: 0
                        , it.userId
                        , it.topicId
                        , it.topicName
                        , it.postId
                        , it.userName
                        , it.avatar
                        , it.teamLogo
                        , it.teamName)
                }


                TOPIC_MESSAGE_NOTIFICATION -> {

                }


                DEBUG_DB -> {
                    TeambrellaUtilService.scheduleDebugDB(context)
                }


                DEBUG_UPDATE -> {
                    TeambrellaUtilService.oneOffUpdate(context, true)
                }

                DEBUG_SYNC -> {
                    TeambrellaUtilService.oneoffWalletSync(context, true, true)
                }

            }
        }

    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {

    }

    override fun onError(ex: Exception?) {

    }
}