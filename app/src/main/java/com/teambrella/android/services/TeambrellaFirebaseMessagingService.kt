package com.teambrella.android.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.backup.WalletBackUpService
import com.teambrella.android.util.TeambrellaUtilService

/**
 * Teambrella FireBase Messaging Service
 */
class TeambrellaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val DEBUG_DB = "101"
        private const val DEBUG_UPDATE = "102"
        private const val DEBUG_SYNC = "103"
        private const val CMD = "Cmd"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            when (it[CMD]) {
                DEBUG_DB -> TeambrellaUtilService.scheduleDebugDB(this)
                DEBUG_SYNC -> TeambrellaUtilService.oneoffWalletSync(this, true)
                DEBUG_UPDATE -> TeambrellaUtilService.oneOffUpdate(this, true)
            }
        }
        WalletBackUpService.schedulePeriodicBackupCheck(this)
    }
}