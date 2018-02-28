package com.teambrella.android.backup

import android.content.Context
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.gcm.*
import com.teambrella.android.ui.TeambrellaUser

/**
 * Wallet Backup Service
 */
class WalletBackUpService : GcmTaskService() {

    companion object {
        private const val CHECK_BACKUP_TAG = "CheckBackup"
        private const val LOG_TAG = "WalletBackupService"

        fun scheduleBackupCheck(context: Context) {
            val task = PeriodicTask.Builder()
                    .setService(WalletBackUpService::class.java)
                    .setTag(CHECK_BACKUP_TAG)
                    .setPersisted(true)
                    .setPeriod((12 * 60 * 60).toLong())
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build()
            GcmNetworkManager.getInstance(context).schedule(task)
        }
    }

    override fun onRunTask(params: TaskParams?): Int {
        when (params?.tag) {
            CHECK_BACKUP_TAG -> onCheckBackUp()
        }
        return GcmNetworkManager.RESULT_SUCCESS
    }

    private fun onCheckBackUp() {
        val user = TeambrellaUser.get(this)
        if (!user.isDemoUser) {
            val googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(Auth.CREDENTIALS_API)
                    .build()
            val connectionResult = googleApiClient.blockingConnect()
            if (connectionResult.isSuccess) {

                googleApiClient.disconnect()
            } else {

            }
        }
    }


}