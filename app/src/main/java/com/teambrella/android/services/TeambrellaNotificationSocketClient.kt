package com.teambrella.android.services

import android.content.Context
import android.net.Uri
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.teambrella.android.api.cmd
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.timeStamp
import com.teambrella.android.services.push.SocketNotificationMessage
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.TeambrellaUtilService
import com.teambrella.android.util.log.Log
import java.lang.Exception
import java.net.URI


const val LOG_TAG = "TeambrellaNotificationSocketClient"

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


    private var socketClient: TeambrellaServer.TeambrellaSocketClient?
    private val gson = GsonBuilder().setLenient().create()
    private val user: TeambrellaUser

    init {
        val uri = URI.create(Uri.Builder()
                .scheme("wss")
                .authority(TeambrellaServer.AUTHORITY)
                .appendEncodedPath("wshandler.ashx")
                .build().toString())

        user = TeambrellaUser.get(context)

        val server = TeambrellaServer(context, user.privateKey
                , user.deviceCode
                , FirebaseInstanceId.getInstance().token
                , user.getInfoMask(context))
        socketClient = server.createSocketClient(uri, this, user.notificationTimeStamp)
        socketClient?.connect()
    }


    override fun onOpen() {
        Log.i(LOG_TAG, "onOpen")
    }

    override fun onMessage(message: String?) {

        message?.let {
            Log.d(LOG_TAG, message)
        }


        val messageObject = message?.let {
            gson.fromJson(it, JsonObject::class.java)
        }


        if (messageObject != null) {
            val timestamp = messageObject.timeStamp
            if (timestamp != null) {
                if (timestamp != -1L && timestamp <= user.notificationTimeStamp) {
                    return
                }
            }

            timestamp?.let { _timestamp ->
                if (_timestamp > 0L) {
                    user.notificationTimeStamp = _timestamp
                }
            }
        }

        messageObject?.let {
            when (it.cmd) {
                CREATED_POST,
                DELETED_POST,
                TYPING,
                NEW_CLAIM,
                PRIVATE_MSG,
                WALLET_FUNDED,
                POSTS_SINCE_INTERACTED,
                NEW_TEAMMATE,
                NEW_DISCUSSION,
                TOPIC_MESSAGE_NOTIFICATION -> {
                    TeambrellaNotificationService.onPushMessage(context, SocketNotificationMessage(it))
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

                else -> Unit

            }

            it.cmd?.let { _cmd ->
                if (_cmd != CREATED_POST && _cmd != DELETED_POST && _cmd != TYPING)
                    StatisticHelper.onPushMessage(context, _cmd.toString(), true)
            }
        }
    }

    fun isClosed() = socketClient?.isClosed ?: true
    fun isClosing() = socketClient?.isClosing

    fun close() {
        socketClient?.close()
        socketClient = null
    }


    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(LOG_TAG, "onClose")
        socketClient?.close()
        socketClient = null
    }

    override fun onError(ex: Exception?) {
        Log.d(LOG_TAG, "onError")
        socketClient?.close()
        socketClient = null
    }
}