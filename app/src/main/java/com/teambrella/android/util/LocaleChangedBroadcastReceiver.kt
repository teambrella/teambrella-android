package com.teambrella.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.teambrella.android.services.TeambrellaNotificationManager

class LocaleChangedBroadcastReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            TeambrellaNotificationManager.recreateNotificationChannels(context)
        }
    }

}