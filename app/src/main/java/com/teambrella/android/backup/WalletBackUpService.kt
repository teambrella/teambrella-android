package com.teambrella.android.backup

import android.content.Context
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.gcm.*
import com.teambrella.android.api.avatar
import com.teambrella.android.api.data
import com.teambrella.android.api.fbName
import com.teambrella.android.api.name
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.image.TeambrellaImageLoader
import com.teambrella.android.services.TeambrellaNotificationManager
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.log.Log

/**
 * Wallet Backup Service
 */
class WalletBackUpService : GcmTaskService() {


    companion object {
        private const val CHECK_BACKUP_PERIODIC_TAG = "CheckBackup"
        private const val CHECK_BACK_UP_ONCE_TAG = "CheckBackupOnce"
        private const val LOG_TAG = "WalletBackupService"
        private const val MAX_NOTIFICATION_DELAY: Long = 7 * 24 * 60 * 60 * 1000 // (a week)
        private const val BACKUP_STATUS_DEMO = "demo"
        private const val BACKUP_STATUS_YES = "backed_up"
        private const val BACKUP_STATUS_NO = "not_backed_up"
        private const val BACKUP_STATUS_NOTIFICATION = "notification"
        private const val BACKUP_STATUS_ERROR = "error";


        fun schedulePeriodicBackupCheck(context: Context) {
            val task = PeriodicTask.Builder()
                    .setService(WalletBackUpService::class.java)
                    .setTag(CHECK_BACKUP_PERIODIC_TAG)
                    .setPersisted(true)
                    .setPeriod((12 * 60 * 60).toLong())
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setUpdateCurrent(false)
                    .build()
            GcmNetworkManager.getInstance(context).schedule(task)
        }

        fun scheduleBackupCheck(context: Context) {
            val task = OneoffTask.Builder()
                    .setService(WalletBackUpService::class.java)
                    .setTag(CHECK_BACK_UP_ONCE_TAG)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 10)
                    .build()
            GcmNetworkManager.getInstance(context).schedule(task)
        }
    }


    override fun onInitializeTasks() {
        super.onInitializeTasks()
        schedulePeriodicBackupCheck(this)
    }

    override fun onRunTask(params: TaskParams?): Int {
        try {
            when (params?.tag) {
                CHECK_BACKUP_PERIODIC_TAG,
                CHECK_BACK_UP_ONCE_TAG -> onCheckBackUp()
            }
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Unable to check backup", e)
            StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_ERROR)
        }
        return GcmNetworkManager.RESULT_SUCCESS
    }

    private fun onCheckBackUp() {
        val user = TeambrellaUser.get(this)
        if (!user.isDemoUser) {
            val server = TeambrellaServer(this, user.privateKey, user.deviceCode, user.getInfoMask(this))
            val me = server.requestObservable(TeambrellaUris.getMe(), null).blockingFirst()
            me?.data?.let {
                val googleApiClient = GoogleApiClient.Builder(this)
                        .addApi(Auth.CREDENTIALS_API, CredentialsOptions.Builder().forceEnableSaveDialog().build())
                        .build()
                val connectionResult = googleApiClient.blockingConnect()
                if (connectionResult.isSuccess) {
                    val credential = Credential.Builder(String.format("fb.com/%s", it.fbName))
                            .setName(it.name)
                            .setPassword(user.privateKey)
                            .setProfilePictureUri(TeambrellaImageLoader.getImageUri(it.avatar))
                            .build()
                    val status = Auth.CredentialsApi.save(googleApiClient, credential).await()
                    when {
                        status.isSuccess -> {
                            user.isWalletBackedUp = true
                            StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_YES)
                        }
                        status.hasResolution() -> {
                            user.isWalletBackedUp = false
                            if (user.canShowBackupNotification(MAX_NOTIFICATION_DELAY)) {
                                TeambrellaNotificationManager(this).showWalletNotBackedUpMessage()
                                user.updateLastBackupNotificationShown()
                                StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_NOTIFICATION)
                            } else {
                                StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_NO)
                            }
                        }
                        else -> {
                            Log.reportNonFatal(LOG_TAG, RuntimeException("unable to write wallet $status"))
                            StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_ERROR)
                        }
                    }
                    googleApiClient.disconnect()
                } else {
                    StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_ERROR)
                    Log.e(LOG_TAG, "unable to connect to google API:$connectionResult")
                }
            }

        } else {
            StatisticHelper.onWalletBackUpCheck(this, BACKUP_STATUS_DEMO)
        }
    }


}