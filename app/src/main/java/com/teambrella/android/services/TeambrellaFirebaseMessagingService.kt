package com.teambrella.android.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teambrella.android.util.log.Log.d

/**
 * Teambrella FireBase Messaging Service
 */
class TeambrellaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val LOG_TAG = "TeambrellaFirebaseMessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        d(LOG_TAG, "From: " + remoteMessage?.from)
        // Check if message contains a data payload.
        if (remoteMessage?.data?.isEmpty() == false) {
            d(LOG_TAG, "Message data payload: " + remoteMessage.data)
        }
        // Check if message contains a notification payload.
        if (remoteMessage?.notification != null) {
            d(LOG_TAG, "Message Notification Body: " + remoteMessage.notification?.body)
        }
    }
}