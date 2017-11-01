package com.teambrella.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.TeambrellaUser;

/**
 * User Present Broadcast Receiver
 */
public class UserPresentBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TeambrellaUser user = TeambrellaUser.get(context);
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_USER_PRESENT.equals(action) && !user.isDemoUser() && user.getPrivateKey() != null) {
            context.startService(new Intent(context, TeambrellaNotificationService.class).setAction(TeambrellaNotificationService.CONNECT_ACTION));
            TeambrellaUtilService.oneoffWalletSync(context);
        }
    }
}
