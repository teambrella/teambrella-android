package com.teambrella.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;

/**
 * Boot Completed broadcast receiver
 */
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = BootCompletedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        TeambrellaUser user = TeambrellaUser.get(context);
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) && !user.isDemoUser() && user.getPrivateKey() != null) {
            try {
                context.startService(new Intent(context, TeambrellaNotificationService.class).setAction(TeambrellaNotificationService.CONNECT_ACTION));
            } catch (Exception e) {
                Log.reportNonFatal(LOG_TAG, e);
            }
        }
    }
}
