package com.teambrella.android.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.chat.ChatActivity;

/**
 * Teambrella Notification Manager
 */
public class TeambrellaNotificationManager {

    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public TeambrellaNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showPrivateMessageNotification(String userId, String userName, String avatar, String text) {
        SpannableString message = new SpannableString(text);
        message.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.darkSkyBlue)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Notification notification = new NotificationCompat.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle(userName)
                .setSmallIcon(R.drawable.ic_teambrella_status)
                .setContentText(message)
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , ChatActivity.getConversationChat(mContext, userId, userName, TeambrellaImageLoader.getImageUri(avatar))
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(4, notification);

    }
}
