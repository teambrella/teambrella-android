package com.teambrella.android.services

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.teambrella.android.util.log.Log

/**
 * Teambrella Firebase Instance ID Service
 */
class TeambrellaFirebaseInstanceIDService : FirebaseInstanceIdService() {
    companion object {
        private const val LOG_TAG = "TeambrellaFirebaseInstanceIDService"
    }

    override fun onTokenRefresh() {
        val token = FirebaseInstanceId.getInstance().token
        Log.e(LOG_TAG, token)
    }
}