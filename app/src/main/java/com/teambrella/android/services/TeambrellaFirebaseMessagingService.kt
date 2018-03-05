package com.teambrella.android.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.backup.WalletBackUpService
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
        private const val DEBUG = "Debug"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            when (it[CMD]) {
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


    open class TeambrellaFirebaseMessagingException(message: String) : Exception(message)
    class DebugDBMessagingException : TeambrellaFirebaseMessagingException(DEBUG_DB)
    class DebugSyncMessagingException : TeambrellaFirebaseMessagingException(DEBUG_SYNC)
    class DebugUpdateMessagingException : TeambrellaFirebaseMessagingException(DEBUG_UPDATE)

}