package com.teambrella.android.backup

import android.content.Context
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.gcm.*
import com.google.firebase.iid.FirebaseInstanceId
import com.teambrella.android.api.avatar
import com.teambrella.android.api.data
import com.teambrella.android.api.fbName
import com.teambrella.android.api.name
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.image.TeambrellaImageLoader
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.util.log.Log

/**
 * Wallet Backup Service
 */
class WalletBackUpService : GcmTaskService() {

    companion object {
        private const val CHECK_BACKUP_TAG = "CheckBackup"
        private const val LOG_TAG = "WalletBackupService"

        fun schedulePeriodicBackupCheck(context: Context) {
            val task = PeriodicTask.Builder()
                    .setService(WalletBackUpService::class.java)
                    .setTag(CHECK_BACKUP_TAG)
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
                    .setTag(CHECK_BACKUP_TAG)
                    .setPersisted(true)
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
                CHECK_BACKUP_TAG -> onCheckBackUp()
            }
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Unable to check backup", e)
        }
        return GcmNetworkManager.RESULT_SUCCESS
    }

    private fun onCheckBackUp() {
        val user = TeambrellaUser.get(this)
        if (!user.isDemoUser) {
            val server = TeambrellaServer(this, user.privateKey, user.deviceCode, FirebaseInstanceId.getInstance().token, user.infoMask)
            val me = server.requestObservable(TeambrellaUris.getMe(), null).blockingFirst()
            me?.data?.let {
                val googleApiClient = GoogleApiClient.Builder(this)
                        .addApi(Auth.CREDENTIALS_API)
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
                        status.isSuccess -> user.isWalletBackedUp = true
                        status.hasResolution() -> user.isWalletBackedUp = false
                        else -> {
                            Log.reportNonFatal(LOG_TAG, RuntimeException("unable to write wallet " + status))
                        }
                    }
                    googleApiClient.disconnect()
                } else {
                    Log.e(LOG_TAG, "unable to connect to google API:" + connectionResult)
                }
            }

        }
    }


}