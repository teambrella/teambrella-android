package com.teambrella.android.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.util.TeambrellaUtilService
import com.teambrella.android.util.log.Log

/**
 * Teambrella FireBase Messaging Service
 */
class TeambrellaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val LOG_TAG = "TeambrellaFirebaseMessagingService"
        private const val DEBUG_DB = "101"
        private const val DEBUG_UPDATE = "102"
        private const val DEBUG_SYNC = "103"
        private const val CMD = "Cmd"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            Log.e(LOG_TAG, "Message data payload:" + it.toString())
            when (it[CMD]) {
                DEBUG_DB -> TeambrellaUtilService.scheduleDebugDB(this)
                DEBUG_SYNC -> TeambrellaUtilService.oneoffWalletSync(this, true)
                DEBUG_UPDATE -> TeambrellaUtilService.oneOffUpdate(this, true)
                else -> TeambrellaUtilService.oneoffWalletSync(this, true)
            }
        }
    }
}