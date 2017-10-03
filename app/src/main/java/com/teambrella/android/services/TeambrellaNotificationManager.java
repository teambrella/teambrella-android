package com.teambrella.android.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;

import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.chat.inbox.InboxActivity;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.ui.widget.AkkuratRegularTypefaceSpan;

import java.util.UUID;

/**
 * Teambrella Notification Manager
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaNotificationManager {

    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public TeambrellaNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressWarnings("ConstantConditions")
    public void showPrivateMessageNotification(String userId, String userName, String avatar, String text) {
        Notification notification = getBuilder()
                .setContentTitle(getTitle(mContext.getString(R.string.private_message_notification_title, userName)))
                .setContentText(getMessage(text))
                .setContentIntent(getChatPendingIntent(userId, userName, avatar))
                .build();
        mNotificationManager.notify(UUID.fromString(userId).hashCode(), notification);
    }

    @SuppressWarnings("ConstantConditions")
    public void showNewClaimNotification(int teamId, int claimId, String name, String amount, String teamName) {
        Notification notification = getBuilder()
                .setContentTitle(getTitle(mContext.getString(R.string.notification_claim_header, teamName)))
                .setContentText(getMessage(mContext.getString(R.string.notification_claim_text, name, amount)))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , ClaimActivity.getLaunchIntent(mContext, claimId, null, teamId)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(claimId, notification);
    }

    @SuppressWarnings("ConstantConditions")
    public void showWalletIsFundedNotification(String amount) {
        Notification notification = getBuilder()
                .setContentTitle(getTitle(mContext.getString(R.string.notification_funded_header)))
                .setContentText(getMessage("+ " + amount + "mETH"))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(17, notification);
    }

    @SuppressWarnings("ConstantConditions")
    public void showNewMessagesSinceLastVisit(int count) {
        Notification notification = getBuilder()
                .setContentTitle(getTitle(mContext.getString(R.string.notification_messages_since, count)))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(19, notification);
    }

    public void showNewTeammates(String name, int othersCount, String teamName) {
        Notification notification = getBuilder()
                .setContentTitle(getTitle(teamName))
                .setContentText(getTitle(othersCount > 0 ? mContext.getResources().getQuantityString(R.plurals.new_teammate_notification_description, othersCount, name, othersCount)
                        : mContext.getString(R.string.new_teammate_notification_description, name)))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getTitle(othersCount > 0 ? mContext.getResources().getQuantityString(R.plurals.new_teammate_notification_description, othersCount, name, othersCount)
                                : mContext.getString(R.string.new_teammate_notification_description, name))))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        mNotificationManager.notify(23, notification);

    }


    private NotificationCompat.Builder getBuilder() {
        return new NotificationCompat.Builder(mContext, null)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_teambrella_status)
                .setColor(mContext.getResources().getColor(R.color.lightBlue))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }


    private SpannableString getTitle(String title) {
        SpannableString spannableTitle = new SpannableString(title);
        spannableTitle.setSpan(new AkkuratBoldTypefaceSpan(mContext), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableTitle;
    }

    private SpannableString getMessage(String message) {
        SpannableString spannableMessage = new SpannableString(message);
        spannableMessage.setSpan(new AkkuratRegularTypefaceSpan(mContext), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableMessage;
    }

    private PendingIntent getChatPendingIntent(String userId, String userName, String avatar) {
        return PendingIntent.getActivities(mContext, 1, new Intent[]
                {new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        , new Intent(mContext, InboxActivity.class)
                        , ChatActivity.getConversationChat(mContext, userId, userName, TeambrellaImageLoader.getImageUri(avatar))
                }, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
