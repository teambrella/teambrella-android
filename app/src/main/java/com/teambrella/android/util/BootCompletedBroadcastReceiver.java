package com.teambrella.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambrella.android.services.TeambrellaNotificationService;

/**
 * Boot Completed broadcast receiver
 */
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            context.startService(new Intent(context, TeambrellaNotificationService.class).setAction(TeambrellaNotificationService.CONNECT_ACTION)
                    .putExtra(TeambrellaNotificationService.EXTRA_TEAM_ID, 2006));
        }
    }
}
