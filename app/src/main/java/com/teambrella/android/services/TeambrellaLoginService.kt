package com.teambrella.android.services

import android.content.Context
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.google.android.gms.gcm.*
import com.teambrella.android.api.TeambrellaServerException
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.blockchain.EtherAccount
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.util.log.Log
import org.bitcoinj.core.DumpedPrivateKey

class TeambrellaLoginService : GcmTaskService() {

    companion object {

        private const val LOG_TAG = "LOG_TAG"
        private const val CHECK_APPLICATION_STATUS_TAG = "CheckApplicationStatus"
        private const val PERIOD: Long = 60

        fun schedulePeriodicAutoLoginTask(context: Context) {
            val task = PeriodicTask.Builder()
                    .setService(TeambrellaLoginService::class.java)
                    .setTag(CHECK_APPLICATION_STATUS_TAG)
                    .setPersisted(true)
                    .setPeriod(PERIOD)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setUpdateCurrent(true)
                    .build()
            GcmNetworkManager.getInstance(context).schedule(task)
        }


        fun autoLogin(context: Context, teamName: String? = null, teamLogo: String?) {
            val user = TeambrellaUser.get(context)
            val accessToken = AccessToken.getCurrentAccessToken()?.token
            if (accessToken != null && (user.privateKey == null || user.isDemoUser)) {
                val privateKey = user.pendingPrivateKey
                val profile: Profile? = Profile.getCurrentProfile()
                val hex = DumpedPrivateKey.fromBase58(null, privateKey).key.publicKeyAsHex
                val server = TeambrellaServer(context, privateKey, user.deviceCode, user.getInfoMask(context))
                val disposable = server.requestObservable(TeambrellaUris.getRegisterUri(accessToken
                        , EtherAccount.toPublicKeySignature(privateKey, context.applicationContext, hex))
                        , null).subscribe({
                    user.privateKey = user.pendingPrivateKey
                    TeambrellaNotificationManager(context).showApplicationApprovedNotification(teamName, teamLogo)
                    LoginManager.getInstance().logOut()
                    GcmNetworkManager.getInstance(context).cancelAllTasks(TeambrellaLoginService::class.java)
                }, { error ->
                    when (error) {
                        is TeambrellaServerException -> {
                            Log.reportNonFatal(LOG_TAG, TeambrellaLoginException(profile, error))
                        }
                    }
                    //
                }, {
                    // nothing to do
                })

                if (disposable?.isDisposed == false) {
                    disposable.dispose()
                }
            }

        }


    }


    override fun onInitializeTasks() {
        super.onInitializeTasks()
        val accessToken = AccessToken.getCurrentAccessToken()
        accessToken?.let {
            schedulePeriodicAutoLoginTask(this)
        }
    }

    override fun onRunTask(params: TaskParams?): Int {
        when (params?.tag) {
            CHECK_APPLICATION_STATUS_TAG -> {
                try {
                    autoLogin()
                } catch (e: Exception) {
                    Log.reportNonFatal(LOG_TAG, e)
                }
            }
        }
        return GcmNetworkManager.RESULT_SUCCESS
    }

    private fun autoLogin() = autoLogin(this, null, null)

    private class TeambrellaLoginException(val profile: Profile?, error: TeambrellaServerException)
        : Exception("${error.localizedMessage} (${profile?.firstName} ${profile?.lastName}) error = ${error.errorCode} ")


}