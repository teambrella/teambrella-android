package com.teambrella.android.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.chat.inbox.InboxActivity;
import com.teambrella.android.ui.claim.ClaimActivity;

import java.util.UUID;

/**
 * Teambrella Notification Manager
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaNotificationManager {

    public enum ChatType {
        APPLICATION,
        CLAIM,
        DISCUSSION
    }


    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public TeambrellaNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressWarnings("ConstantConditions")
    public void showPrivateMessageNotification(String userId, String userName, String avatar, String text) {
        Notification notification = getBuilder()
                .setContentTitle(mContext.getString(R.string.private_message_notification_title, userName))
                .setContentText(text)
                .setContentIntent(getChatPendingIntent(userId, userName, avatar))
                .build();
        mNotificationManager.notify(UUID.fromString(userId).hashCode(), notification);
    }

    @SuppressWarnings("ConstantConditions")
    public void showNewClaimNotification(int teamId, int claimId, String name, String amount, String teamName) {
        Notification notification = getBuilder()
                .setContentTitle(mContext.getString(R.string.notification_claim_header, teamName))
                .setContentText(mContext.getString(R.string.notification_claim_text, name, amount))
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
                .setContentTitle(mContext.getString(R.string.notification_funded_header))
                .setContentText("+ " + amount + " mETH")
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class).setAction(MainActivity.ACTION_SHOW_WALLET)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(17, notification);
    }

    @SuppressWarnings("ConstantConditions")
    public void showNewMessagesSinceLastVisit(int count) {
        Notification notification = getBuilder()
                .setContentTitle(mContext.getString(R.string.notification_messages_since, count))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class).setAction(MainActivity.ACTION_SHOW_FEED)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(19, notification);
    }

    public void showNewTeammates(String name, int othersCount, String teamName) {
        Notification notification = getBuilder()
                .setContentTitle(teamName)
                .setContentText(othersCount > 0 ? mContext.getResources().getQuantityString(R.plurals.new_teammate_notification_description, othersCount, name, othersCount)
                        : mContext.getString(R.string.new_teammate_notification_description, name))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(othersCount > 0 ? mContext.getResources().getQuantityString(R.plurals.new_teammate_notification_description, othersCount, name, othersCount)
                                : mContext.getString(R.string.new_teammate_notification_description, name)))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        mNotificationManager.notify(23, notification);

    }

    public void showNewDiscussion(String teamName, String userName, int teamId, String topicName, String topicId) {
        Notification notification = getBuilder()
                .setContentTitle(teamName)
                .setContentText(mContext.getString(R.string.new_discussion_notification_description, userName, topicName))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mContext.getString(R.string.new_discussion_notification_description, userName, topicName)))
                .setContentIntent(PendingIntent.getActivities(mContext
                        , 1
                        , new Intent[]{new Intent(mContext, MainActivity.class), ChatActivity.getFeedChat(mContext, topicName,
                                topicId, teamId, TeambrellaModel.TeamAccessLevel.FULL_ACCESS)}
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        mNotificationManager.notify(29, notification);
    }


    /**
     * Cancel chat notification
     *
     * @param topicId topic Id
     */
    public void cancelChatNotification(String topicId) {
        mNotificationManager.cancel(topicId.hashCode());
    }


    public void showNewPublicChatMessage(ChatType type, String title, String sender, String text, boolean userTopic, String topicId, Intent intent) {
        NotificationCompat.Builder builder = getBuilder().setStyle(new NotificationCompat.BigTextStyle()
                .bigText(mContext.getString(R.string.notification_chat_text_format_string, sender, text)))
                .setContentText(mContext.getString(R.string.notification_chat_text_format_string, sender, text))
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 0
                        , intent
                        , PendingIntent.FLAG_UPDATE_CURRENT));

        switch (type) {
            case APPLICATION: {
                builder.setContentTitle(userTopic ? mContext.getString(R.string.notification_title_your_application) :
                        mContext.getString(R.string.notification_title_other_application, title));
                builder.setContentIntent(PendingIntent.getActivity(mContext
                        , 0
                        , intent
                        , PendingIntent.FLAG_UPDATE_CURRENT));
            }
            break;

            case CLAIM: {
                builder.setContentTitle(userTopic ? mContext.getString(R.string.notification_title_your_claim) :
                        mContext.getString(R.string.notification_title_other_claim, title));
            }
            break;

            case DISCUSSION: {
                builder.setContentTitle(title);
            }

            break;
        }
        mNotificationManager.notify(topicId.hashCode(), builder.build());
    }


    private NotificationCompat.Builder getBuilder() {
        return new NotificationCompat.Builder(mContext, null)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_teambrella_status)
                .setColor(mContext.getResources().getColor(R.color.lightBlue))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }


    private PendingIntent getChatPendingIntent(String userId, String userName, String avatar) {
        return PendingIntent.getActivities(mContext, 1, new Intent[]
                {new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        , new Intent(mContext, InboxActivity.class)
                        , ChatActivity.getConversationChat(mContext, userId, userName, TeambrellaImageLoader.getImageUri(avatar))
                }, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
