package com.teambrella.android.services;


import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.image.glide.GlideRequest;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.WelcomeActivity;
import com.teambrella.android.ui.background.BackgroundRestrictionsActivity;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.chat.inbox.InboxActivity;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.teambrella.android.services.push.KPushNotifications.APPLICATION_STARTED;
import static com.teambrella.android.services.push.KPushNotifications.NEW_CLAIM;
import static com.teambrella.android.services.push.KPushNotifications.NEW_DISCUSSION;
import static com.teambrella.android.services.push.KPushNotifications.NEW_TEAMMATE;
import static com.teambrella.android.services.push.KPushNotifications.POSTS_SINCE_INTERACTED;
import static com.teambrella.android.services.push.KPushNotifications.PRIVATE_MSG;
import static com.teambrella.android.services.push.KPushNotifications.PROXY;
import static com.teambrella.android.services.push.KPushNotifications.PROXY_SEED;
import static com.teambrella.android.services.push.KPushNotifications.TOPIC_MESSAGE_NOTIFICATION;
import static com.teambrella.android.services.push.KPushNotifications.WALLET_FUNDED;

/**
 * Teambrella Notification Manager
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaNotificationManager {


    private static final String PICTURE_PREFIX = "/ImageHandler.ashx";
    private static final String LOG_TAG = TeambrellaNotificationManager.class.getSimpleName();

    private static final String PRIVATE_CHATS_ID = "private_chats";
    private static final String SUBSCRIBED_CHATS_ID = "subscribed_chats";
    private static final String NEW_CLAIMS_ID = "new_claims";
    private static final String WALLET_ID = "wallet";
    private static final String TEAM_UPDATES_ID = "team_updates";
    private static final String CHATS_ID = "chats";


    private static final int NEW_TEAMMATES_NOTIFICATION_ID = 23;
    private static final int NEW_DISCUSSION_NOTIFICATION_ID = 29;
    private static final int USER_IS_PROXY_NOTIFICATION_ID = 37;
    private static final int BACKGROUND_RESTRICTION_NOTIFICATION_ID = 43;
    private static final int APPLICATION_APPROVED_ID = 47;
    private static final int APPLICATION_STARTED_ID = 53;


    public enum ChatType {
        APPLICATION,
        CLAIM,
        DISCUSSION
    }


    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final TeambrellaUser mUser;
    private final TeambrellaImageLoader mImageLoader;

    public static void recreateNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                List<NotificationChannel> list = new LinkedList<>();
                NotificationChannelGroup chatGroup = new NotificationChannelGroup(CHATS_ID, context.getString(R.string.notification_group_chats));

                notificationManager.createNotificationChannelGroup(chatGroup);


                NotificationChannel chat = new NotificationChannel(PRIVATE_CHATS_ID
                        , context.getString(R.string.notification_channel_private_chats)
                        , NotificationManager.IMPORTANCE_DEFAULT);
                chat.setGroup(CHATS_ID);
                list.add(chat);

                NotificationChannel subscribedChats = new NotificationChannel(SUBSCRIBED_CHATS_ID
                        , context.getString(R.string.notification_channel_subscribed_chats)
                        , NotificationManager.IMPORTANCE_DEFAULT);

                subscribedChats.setGroup(CHATS_ID);
                list.add(subscribedChats);

                list.add(new NotificationChannel(NEW_CLAIMS_ID
                        , context.getString(R.string.notification_channel_new_claims)
                        , NotificationManager.IMPORTANCE_DEFAULT));

                list.add(new NotificationChannel(WALLET_ID
                        , context.getString(R.string.notification_channel_wallet)
                        , NotificationManager.IMPORTANCE_DEFAULT));

                list.add(new NotificationChannel(TEAM_UPDATES_ID
                        , context.getString(R.string.notification_channel_team_updates)
                        , NotificationManager.IMPORTANCE_DEFAULT));

                notificationManager.createNotificationChannels(list);

            }


        }
    }


    public TeambrellaNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mUser = TeambrellaUser.get(mContext);
        TeambrellaServer mServer = new TeambrellaServer(mContext, mUser.getPrivateKey(), mUser.getDeviceCode(), mUser.getInfoMask(mContext));
        mImageLoader = new TeambrellaImageLoader(mServer);
        recreateNotificationChannels(context);
    }

    private void showPrivateMessageNotification(String userId, String userName, String avatar, String text, String title, String subtitle) {
        final int id = UUID.fromString(userId).hashCode();
        notifyUser(id
                , title != null ? title : userName
                , subtitle != null ? subtitle : text
                , getAvatarRequest(avatar)
                , getBuilder(PRIVATE_CHATS_ID).setContentIntent(getChatPendingIntent(id, userId, userName, avatar)));
    }

    private void showNewClaimNotification(int claimId, String name, String amount, String avatar, int teamId, String teamName, Intent intent
            , String title, String subtitle) {
        notifyUser(claimId
                , title != null ? title : mContext.getString(R.string.notification_claim_header, teamName)
                , subtitle != null ? subtitle : mContext.getString(R.string.notification_claim_text, name, amount)
                , getAvatarRequest(avatar)
                , getBuilder(NEW_CLAIMS_ID).setContentIntent(
                        PendingIntent.getActivity(mContext
                                , 1
                                , intent
                                , PendingIntent.FLAG_UPDATE_CURRENT)));
    }


    public void showApplicationApprovedNotification(@Nullable String teamName, @Nullable String teamIcon) {
        if (teamName != null && teamIcon != null) {
            notifyUser(APPLICATION_APPROVED_ID, teamName, mContext.getString(R.string.application_approved_notification_description), getTeamIconRequest(teamIcon)
                    , getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext, 1
                            , new Intent(mContext, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT)));
        } else {
            notifyUser(APPLICATION_APPROVED_ID, mContext.getString(R.string.app_name), mContext.getString(R.string.application_approved_notification_description), (Bitmap) null
                    , getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext, 1
                            , new Intent(mContext, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT)));
        }
    }

    private void showApplicationStartedNotification(@Nullable String teamName, @Nullable String teamIcon
            , String title, String subtitle) {
        if (teamName != null && teamIcon != null) {
            notifyUser(APPLICATION_STARTED_ID, title != null ? title : teamName, subtitle != null ? subtitle : mContext.getString(R.string.application_started_notification_description), getTeamIconRequest(teamIcon)
                    , getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext, 1
                            , new Intent(mContext, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT)));
        } else {
            notifyUser(APPLICATION_STARTED_ID, title != null ? title : mContext.getString(R.string.app_name)
                    , subtitle != null ? subtitle : mContext.getString(R.string.application_started_notification_description), (Bitmap) null
                    , getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext, 1
                            , new Intent(mContext, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT)));
        }
    }


    public void showBackgroundRestrictionsNotification() {
        notifyUser(BACKGROUND_RESTRICTION_NOTIFICATION_ID, mContext.getString(R.string.huawei_protected_app_action), mContext.getString(R.string.huawei_protected_app_description),
                (Bitmap) null, getBuilder(WALLET_ID).setContentIntent(
                        PendingIntent.getActivity(mContext
                                , 1
                                , BackgroundRestrictionsActivity.Companion.getNotificationIntent(mContext)
                                , PendingIntent.FLAG_UPDATE_CURRENT)));
    }


    private void showNewTeammateJoined(String userId, String userName, String avatar, int teamId, String teamName, String topicId,
                                       String title, String subtitle) {
        notifyUser(topicId.hashCode(),
                title != null ? title : teamName,
                subtitle != null ? subtitle : mContext.getString(R.string.notification_description_new_teammate_joined, userName),
                getAvatarRequest(avatar),
                getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , ChatActivity.getTeammateChat(mContext
                                , teamId
                                , userId
                                , userName
                                , avatar
                                , topicId
                                , TeambrellaModel.TeamAccessLevel.FULL_ACCESS)
                                .putExtra(ATeambrellaActivity.EXTRA_BACK_PRESSED_INTENT, getMainActivityPendingIntent(MainActivity.ACTION_SHOW_FEED, teamId))
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }


    private void showUserIsProxyFor(String userName, int teamId, String teamName, String avatar, boolean male, String title, String subtitle) {
        notifyUser(USER_IS_PROXY_NOTIFICATION_ID
                , title != null ? title : teamName
                , subtitle != null ? subtitle : mContext.getString(male ? R.string.notification_description_your_are_proxy_male : R.string.notification_description_your_are_proxy_female, userName)
                , getAvatarRequest(avatar)
                , getBuilder(WALLET_ID).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , MainActivity.getLaunchIntent(mContext, MainActivity.ACTION_SHOW_I_AM_PROXY_FOR, teamId)
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }


    private void showWalletIsFundedNotification(String amount, int teamId, String teamIcon, String title, String subtitle) {
        notifyUser(WALLET_ID.hashCode()
                , title != null ? title : mContext.getString(R.string.notification_funded_header)
                , subtitle != null ? subtitle : "+ " + amount + " mETH"
                , getTeamIconRequest(teamIcon)
                , getBuilder(WALLET_ID).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , MainActivity.getLaunchIntent(mContext, MainActivity.ACTION_SHOW_WALLET, teamId)
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }

    private void showNewMessagesSinceLastVisit(int count, String teamName, String teamIcon, int teamId, String title, String subtitle) {
        notifyUser(TEAM_UPDATES_ID.hashCode()
                , title != null ? title : teamName
                , subtitle != null ? subtitle : mContext.getResources().getQuantityString(R.plurals.new_comments_since_yesterday, count, count)
                , getTeamIconRequest(teamIcon)
                , getBuilder(TEAM_UPDATES_ID).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , MainActivity.getLaunchIntent(mContext, MainActivity.ACTION_SHOW_FEED, teamId)
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }

    private void showNewTeammates(String name, int othersCount, String teamName, String teamLogo, String title, String subtitle) {
        notifyUser(NEW_TEAMMATES_NOTIFICATION_ID, title != null ? title : teamName
                , subtitle != null ? subtitle : (othersCount > 0 ? mContext.getResources().getQuantityString(R.plurals.new_teammate_notification_description, othersCount, name, othersCount)
                        : mContext.getString(R.string.new_teammate_notification_description, name))
                , getTeamIconRequest(teamLogo)
                , getBuilder(null).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , new Intent(mContext, MainActivity.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }

    private void showNewDiscussion(String teamName, String userName, String avatar, int teamId, String topicName
            , String topicId, String title, String subtitle) {
        notifyUser(NEW_DISCUSSION_NOTIFICATION_ID, title != null ? title : teamName
                , subtitle != null ? subtitle : mContext.getString(R.string.new_discussion_notification_description, userName, topicName)
                , getAvatarRequest(avatar)
                , getBuilder(null).setContentIntent(PendingIntent.getActivities(mContext
                        , 1
                        , new Intent[]{new Intent(mContext, MainActivity.class), ChatActivity.getFeedChat(mContext, topicName,
                                topicId, teamId, TeambrellaModel.TeamAccessLevel.FULL_ACCESS)}
                        , PendingIntent.FLAG_UPDATE_CURRENT))
        );
    }


    public void showWalletNotBackedUpMessage() {
        notifyUser(WALLET_ID.hashCode()
                , mContext.getString(R.string.your_wallet_not_backed_up)
                , mContext.getString(R.string.tap_to_backup_wallet)
                , (GlideRequest<Bitmap>) null
                , getBuilder(WALLET_ID).setContentIntent(PendingIntent.getActivity(mContext
                        , 1
                        , MainActivity.getLaunchIntent(mContext, MainActivity.ACTION_BACKUP_WALLET, -1)
                        , PendingIntent.FLAG_UPDATE_CURRENT)));
    }


    /**
     * Cancel chat notification
     *
     * @param topicId topic Id
     */
    public void cancelChatNotification(String topicId) {
        mNotificationManager.cancel(topicId.hashCode());
    }

    /**
     * Cancel private chat notification
     *
     * @param userId user Id
     */
    public void cancelPrivateChatNotification(String userId) {
        mNotificationManager.cancel(UUID.fromString(userId).hashCode());
    }


    public void handlePushMessage(INotificationMessage message) {
        switch (message.getCmd()) {
            case NEW_CLAIM:
                showNewClaimNotification(message.getClaimId(), message.getSenderUserName()
                        , message.getAmount(), message.getSenderAvatar(), message.getTeamId(), message.getTeamName()
                        , ChatActivity.getClaimChat(mContext
                                , message.getTeamId()
                                , message.getClaimId()
                                , message.getClaimObjectName()
                                , message.getClaimPhoto()
                                , message.getTopicId()
                                , TeambrellaModel.TeamAccessLevel.FULL_ACCESS
                                , null).putExtra(ATeambrellaActivity.EXTRA_BACK_PRESSED_INTENT, getMainActivityPendingIntent(MainActivity.ACTION_SHOW_FEED, message.getTeamId()))
                        , message.getTitle()
                        , message.getSubtitle());
                break;
            case POSTS_SINCE_INTERACTED:
                showNewMessagesSinceLastVisit(message.getCount(), message.getTeamName(), message.getTeamLogo()
                        , message.getTeamId(), message.getTitle(), message.getSubtitle());
                break;
            case PRIVATE_MSG:
                showPrivateMessageNotification(message.getSenderUserId(), message.getSenderUserName(), message.getSenderAvatar(), message.getMessage()
                        , message.getTitle(), message.getSubtitle());
                break;
            case NEW_DISCUSSION:
                showNewDiscussion(message.getTeamName(), message.getSenderUserName(), message.getSenderAvatar(), message.getTeamId(), message.getTopicName()
                        , message.getTopicId(), message.getTitle(), message.getSubtitle());
                break;
            case NEW_TEAMMATE:
                showNewTeammates(message.getSenderUserName(), message.getCount(), message.getTeamName()
                        , message.getTeamLogo(), message.getTitle(), message.getSubtitle());
                break;
            case WALLET_FUNDED:
                showWalletIsFundedNotification(message.getBalanceCrypto(), message.getTeamId(), message.getTeamLogo()
                        , message.getTitle(), message.getSubtitle());
                break;
            case PROXY_SEED:
                showNewTeammateJoined(message.getSenderUserId(), message.getSenderUserName()
                        , message.getSenderAvatar(), message.getTeamId(), message.getTeamName()
                        , message.getTopicId(), message.getTitle(), message.getSubtitle());
                break;
            case PROXY:
                showUserIsProxyFor(message.getSenderUserName(), message.getTeamId()
                        , message.getTeamName(), message.getSenderAvatar(), message.isMale(), message.getTitle(), message.getSubtitle());
                break;
            case APPLICATION_STARTED:
                showApplicationStartedNotification(message.getTeamName(), message.getTeamLogo(), message.getTitle(), message.getSubtitle());
                break;
            case TOPIC_MESSAGE_NOTIFICATION:
                String senderUserId = message.getSenderUserId();
                if (senderUserId != null && senderUserId.equalsIgnoreCase(mUser.getUserId())) {
                    break;
                }

                if (message.getTeammateUserId() != null) {
                    showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.APPLICATION
                            , message.getTeammateUserName()
                            , message.getSenderUserName()
                            , message.getContent()
                            , message.getSenderAvatar()
                            , message.isMyTopic()
                            , message.getTeamId()
                            , message.getTopicId()
                            , ChatActivity.getTeammateChat(mContext
                                    , message.getTeamId()
                                    , message.getTeammateUserId()
                                    , message.getTeammateUserName()
                                    , message.getTeammateAvatar()
                                    , message.getTopicId()
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS)
                            , message.getTitle()
                            , message.getSubtitle());
                } else if (message.getClaimId() != 0) {
                    showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.CLAIM
                            , message.getClaimerName()
                            , message.getSenderUserName()
                            , message.getContent()
                            , message.getSenderAvatar()
                            , message.isMyTopic()
                            , message.getTeamId()
                            , message.getTopicId()
                            , ChatActivity.getClaimChat(mContext
                                    , message.getTeamId()
                                    , message.getClaimId()
                                    , message.getClaimObjectName()
                                    , message.getClaimPhoto()
                                    , message.getTopicId()
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS
                                    , null)
                            , message.getTitle()
                            , message.getSubtitle());
                } else if (message.getDiscussionTopicName() != null) {
                    showNewPublicChatMessage(TeambrellaNotificationManager.ChatType.DISCUSSION
                            , message.getDiscussionTopicName()
                            , message.getSenderUserName()
                            , message.getContent()
                            , message.getSenderAvatar()
                            , message.isMyTopic()
                            , message.getTeamId()
                            , message.getTopicId()
                            , ChatActivity.getFeedChat(mContext
                                    , message.getDiscussionTopicName()
                                    , message.getTopicId()
                                    , message.getTeamId()
                                    , TeambrellaModel.TeamAccessLevel.FULL_ACCESS)
                            , message.getTitle()
                            , message.getSubtitle());
                }
                break;
        }
    }


    private void showNewPublicChatMessage(ChatType type, String receiver, String sender, String text, String senderAvatar
            , boolean userTopic, int teamId, String topicId, Intent intent, String title, String subtitle) {
        String notificationText = subtitle != null ? subtitle : (text != null && text.startsWith(PICTURE_PREFIX) ? mContext.getString(R.string.notification_chat_picture_format_string, sender)
                : mContext.getString(R.string.notification_chat_text_format_string, sender, text));
        String contentTitle = null;
        NotificationCompat.Builder builder = getBuilder(SUBSCRIBED_CHATS_ID)
                .setContentIntent(PendingIntent.getActivity(mContext
                        , 0
                        , intent.putExtra(ATeambrellaActivity.EXTRA_BACK_PRESSED_INTENT, getMainActivityPendingIntent(MainActivity.ACTION_SHOW_FEED, teamId))
                        , PendingIntent.FLAG_UPDATE_CURRENT));

        switch (type) {
            case APPLICATION: {
                contentTitle = title != null ? title : (userTopic ? mContext.getString(R.string.notification_title_your_application) :
                        mContext.getString(R.string.notification_title_other_application, receiver));
            }
            break;

            case CLAIM: {
                contentTitle = title != null ? title : (userTopic ? mContext.getString(R.string.notification_title_your_claim) :
                        mContext.getString(R.string.notification_title_other_claim, receiver));
            }
            break;

            case DISCUSSION: {
                contentTitle = receiver;
            }

            break;
        }

        notifyUser(topicId.hashCode(),
                contentTitle,
                notificationText,
                getAvatarRequest(senderAvatar),
                builder);
    }


    private NotificationCompat.Builder getBuilder(String channelId) {
        return new NotificationCompat.Builder(mContext, channelId != null ? channelId : "")
                .setAutoCancel(true)
                .setColor(mContext.getResources().getColor(R.color.lightBlue))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }


    private PendingIntent getChatPendingIntent(int requestCode, String userId, String userName, String avatar) {
        return PendingIntent.getActivities(mContext, requestCode, new Intent[]
                {new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        , new Intent(mContext, InboxActivity.class)
                        , ChatActivity.getConversationChat(mContext, userId, userName, avatar)
                }, 0);
    }


    private void notifyUser(final int id, final String title, final String subtitle, GlideRequest<Bitmap> imageRequest, final NotificationCompat.Builder builder) {
        if (imageRequest != null) {
            imageRequest.into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    notifyUser(id, title, subtitle, resource, builder);
                }
                @Override
                public void onLoadFailed(Drawable errorDrawable) {
                    notifyUser(id, title, subtitle, (Bitmap) null, builder);
                }

            });
        }
        else {
            notifyUser(id, title, subtitle, (Bitmap) null, builder);
        }

    }


    private GlideRequest<Bitmap> getAvatarRequest(String imageUrl) {
        return GlideApp.with(mContext).asBitmap().load(mImageLoader.getImageUrl(imageUrl))
                .apply(RequestOptions.circleCropTransform());
    }

    private GlideRequest<Bitmap> getTeamIconRequest(String imageUrl) {
        return GlideApp.with(mContext).asBitmap().load(mImageLoader.getImageUrl(imageUrl))
                .apply(new RequestOptions().transforms(new CenterCrop()
                        , new RoundedCorners(mContext.getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp))));
    }


    private void notifyUser(int id, String title, String subtitle, Bitmap
            iconBitmap, NotificationCompat.Builder builder) {
        builder.setContentTitle(title)
                .setContentText(subtitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(subtitle).setBigContentTitle(title))
                .setSmallIcon(R.drawable.ic_teambrella_status)
                .setLargeIcon(iconBitmap);
        mNotificationManager.notify(id, builder.build());
    }

    private PendingIntent getMainActivityPendingIntent(@SuppressWarnings("SameParameterValue") String action, int teamId) {
        return PendingIntent.getActivity(mContext, 1
                , MainActivity.getLaunchIntent(mContext, action, teamId), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
