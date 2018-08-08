package com.teambrella.android.ui.chat;

import android.annotation.SuppressLint;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaClientException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.services.TeambrellaNotificationManager;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivityKt;
import com.teambrella.android.ui.base.TeambrellaBroadcastManager;
import com.teambrella.android.ui.base.TeambrellaDataViewModel;
import com.teambrella.android.ui.base.TeambrellaPagerViewModel;
import com.teambrella.android.ui.claim.IClaimActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.ImagePicker;
import com.teambrella.android.util.StatisticHelper;
import com.teambrella.android.util.TeambrellaDateUtils;

import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

import io.reactivex.Notification;
import io.reactivex.Observable;

import static com.teambrella.android.services.push.KPushNotifications.CREATED_POST;
import static com.teambrella.android.services.push.KPushNotifications.PRIVATE_MSG;
import static com.teambrella.android.services.push.KPushNotifications.TOPIC_MESSAGE_NOTIFICATION;

/**
 * Claim chat
 */
public class ChatActivity extends ATeambrellaActivity implements IChatActivity, IClaimActivity {

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_TOPIC_ID = "topicId";
    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_USER_NAME = "user_name";
    private static final String EXTRA_IMAGE_URI = "image_uri";
    private static final String EXTRA_CLAIM_ID = "claim_id";
    private static final String EXTRA_OBJECT_NAME = "object_name";
    private static final String EXTRA_TEAM_ACCESS_LEVEL = "team_access_level";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_DATE = "date";
    private static final String EXTRA_LAST_READ = "las_read";
    private static final String EXTRA_ACTION = "chat_action";


    private static final String DATA_FRAGMENT_TAG = "data_fragment_tag";
    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";
    public static final String CLAIM_DATA_TAG = "claim_data_tag";
    public static final String VOTE_DATA_TAG = "vote_data_tag";
    private static final String NOTIFICATION_SETTINGS_FRAGMENT_TAG = "notification_settings";

    private static final String SHOW_TEAMMATE_CHAT_ACTION = "show_teammate_chat_action";
    private static final String SHOW_CLAIM_CHAT_ACTION = "show_claim_chat_action";
    private static final String SHOW_FEED_CHAT_ACTION = "show_feed_chat_action";
    private static final String SHOW_CONVERSATION_CHAT = "show_conversation_chat_action";

    private Uri mUri;
    private String mTopicId;
    private String mAction;
    private String mUserId;
    private int mTeamId;

    private TextView mMessageView;
    private ImagePicker mImagePicker;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mIcon;
    private ChatNotificationClient mClient;
    private TeambrellaNotificationManager mNotificationManager;
    private View mNotificationHelpView;
    private MuteStatus mMuteStatus = null;
    private float mVote = -1;
    private long mLastRead = -1L;

    private TeambrellaBroadcastManager mChatBroadCastManager;


    public static void startConversationChat(Context context, String userId, String userName, String imageUri) {
        context.startActivity(getConversationChat(context, userId, userName, imageUri));
    }

    public static Intent getConversationChat(Context context, String userId, String userName, String imageUri) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_USER_ID, userId)
                .putExtra(EXTRA_URI, TeambrellaUris.getConversationChat(userId))
                .putExtra(EXTRA_USER_NAME, userName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_ACTION, SHOW_CONVERSATION_CHAT)
                .setAction(SHOW_CONVERSATION_CHAT + userId);
    }

    public static Intent getClaimChat(Context context, int teamId, int claimId, String objectName, String imageUri, String topicId, int accessLevel, String date) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_CLAIM_ID, claimId)
                .putExtra(EXTRA_OBJECT_NAME, objectName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_URI, TeambrellaUris.getClaimChatUri(claimId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .putExtra(EXTRA_DATE, date)
                .putExtra(EXTRA_ACTION, SHOW_CLAIM_CHAT_ACTION)
                .setAction(SHOW_CLAIM_CHAT_ACTION + topicId);
    }

    public static Intent getTeammateChat(Context context, int teamId, String userId, String userName, String imageUri, String topicId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_USER_ID, userId)
                .putExtra(EXTRA_USER_NAME, userName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_URI, TeambrellaUris.getTeammateChatUri(teamId, userId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .putExtra(EXTRA_ACTION, SHOW_TEAMMATE_CHAT_ACTION)
                .setAction(SHOW_TEAMMATE_CHAT_ACTION + topicId);
    }


    public static Intent getFeedChat(Context context, String title, String topicId, int teamId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URI, TeambrellaUris.getFeedChatUri(topicId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .putExtra(EXTRA_ACTION, SHOW_FEED_CHAT_ACTION)
                .setAction(SHOW_FEED_CHAT_ACTION + topicId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_window_background));

        mChatBroadCastManager = new TeambrellaBroadcastManager(this);

        mUri = intent.getParcelableExtra(EXTRA_URI);
        mTopicId = intent.getStringExtra(EXTRA_TOPIC_ID);
        mUserId = intent.getStringExtra(EXTRA_USER_ID);
        mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
        int mClaimId = intent.getIntExtra(EXTRA_CLAIM_ID, 0);
        mAction = intent.getStringExtra(EXTRA_ACTION);

        mLastRead = savedInstanceState != null ? savedInstanceState.getLong(EXTRA_LAST_READ, -1) : -1;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getComponent().inject(this);

        mNotificationManager = new TeambrellaNotificationManager(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
            if (mAction != null && mAction.equals(SHOW_CONVERSATION_CHAT)) {
                actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setCustomView(R.layout.chat_toolbar_view);
                View view = actionBar.getCustomView();
                mTitle = view.findViewById(R.id.title);
                mSubtitle = view.findViewById(R.id.subtitle);
                mIcon = view.findViewById(R.id.icon);
                Toolbar parent = (Toolbar) view.getParent();
                parent.setPadding(0, 0, 0, 0);
                parent.setContentInsetsAbsolute(0, 0);
            }

        }

        mImagePicker = new ImagePicker(this);
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT_TAG) == null) {
            transaction.add(R.id.container, ADataFragmentKt.createDataFragment(new String[]{DATA_FRAGMENT_TAG}, KChatFragment.class), UI_FRAGMENT_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }


        mNotificationHelpView = findViewById(R.id.notification_help);

        mMessageView = findViewById(R.id.text);
        findViewById(R.id.send_text).setOnClickListener(this::onClick);
        findViewById(R.id.send_image).setOnClickListener(this::onClick);


        if (mAction != null) {
            switch (mAction) {
                case SHOW_TEAMMATE_CHAT_ACTION:
                    setTitle(R.string.application);
                    break;

                case SHOW_CLAIM_CHAT_ACTION: {
                    String incidentDate = intent.getStringExtra(EXTRA_DATE);
                    if (incidentDate != null) {
                        setClaimTitle(incidentDate);
                    } else {
                        setTitle(R.string.claim);
                    }

                }
                break;
                case SHOW_CONVERSATION_CHAT:
                    setTitle(R.string.private_conversation);
                    findViewById(R.id.send_image).setVisibility(View.GONE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMessageView.getLayoutParams();
                    params.leftMargin = getResources().getDimensionPixelSize(R.dimen.margin_8);
                    mMessageView.setLayoutParams(params);
                    if (mTitle != null) {
                        mTitle.setText(R.string.private_conversation);
                    }

                    if (mSubtitle != null) {
                        mSubtitle.setText(intent.getStringExtra(EXTRA_USER_NAME));
                    }

                    String mImageUri = intent.getStringExtra(EXTRA_IMAGE_URI);
                    if (mImageUri != null && mIcon != null) {
                        GlideApp.with(this).load(getImageLoader().getImageUrl(mImageUri))
                                .apply(new RequestOptions().transforms(new CenterCrop(), new CircleCrop())
                                        .placeholder(R.drawable.picture_background_circle))
                                .into(mIcon);
                        //mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, intent.getStringExtra(EXTRA_USER_NAME), mImageUri));
                    }
                    break;

                case SHOW_FEED_CHAT_ACTION:
                    setTitle(intent.getStringExtra(EXTRA_TITLE));
                    break;
            }
        }

        switch (intent.getIntExtra(EXTRA_TEAM_ACCESS_LEVEL, TeambrellaModel.TeamAccessLevel.FULL_ACCESS)) {
            case TeambrellaModel.TeamAccessLevel.FULL_ACCESS:
                findViewById(R.id.input).setVisibility(View.VISIBLE);
                break;
            default:
                if (mUserId != null && mUserId.equals(TeambrellaUser.get(this).getUserId())) {
                    findViewById(R.id.input).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.input).setVisibility(View.GONE);
                }
                break;
        }


        mClient = new ChatNotificationClient(this);
        mClient.connect();
        setResult(RESULT_OK);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getPager(DATA_FRAGMENT_TAG).getDataObservable()
                .observe(this, this::onDataUpdated);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.disconnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAction == null || !mAction.equals(SHOW_CONVERSATION_CHAT)) {
            if (mMuteStatus != null) {
                switch (mMuteStatus) {
                    case DEFAULT:
                    case MUTED:
                        menu.add(0, R.id.unmute, 0, null)
                                .setIcon(R.drawable.ic_icon_bell_muted).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        break;
                    case UMMUTED:
                        menu.add(0, R.id.mute, 0, null)
                                .setIcon(R.drawable.ic_icon_bell).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        break;

                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_text:
                String text = mMessageView.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    ChatViewModel model = ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel.class);
                    switch (mAction) {
                        case SHOW_CONVERSATION_CHAT: {
                            String uuid = UUID.randomUUID().toString();
                            model.addPendingMessage(uuid, text, -1f);
                            request(TeambrellaUris.getNewConversationMessage(mUserId, uuid, text));
                            StatisticHelper.onPrivateMessage(this);
                        }
                        break;
                        default: {
                            String uuid = UUID.randomUUID().toString();
                            model.addPendingMessage(uuid, text, mVote);
                            request(TeambrellaUris.getNewPostUri(mTopicId, uuid, text, null));
                            StatisticHelper.onChatMessage(this, mTeamId, mTopicId, StatisticHelper.MESSAGE_TEXT);
                        }
                    }
                }
                mMessageView.setText(null);
                break;
            case R.id.send_image:
                mImagePicker.startPicking();
                break;
        }
    }

    @Override
    public Uri getChatUri() {
        return getIntent().getParcelableExtra(EXTRA_URI);
    }

    protected void onRequestResult(Notification<JsonObject> response) {
        if (response.isOnNext()) {

            String requestUriString = Observable.just(response.getValue()).map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI))
                    .blockingFirst(null);
            Uri uri = Uri.parse(requestUriString);
            switch (TeambrellaUris.sUriMatcher.match(uri)) {
                case TeambrellaUris.NEW_FILE:
                    JsonArray array = Observable.just(response.getValue()).map(JsonWrapper::new)
                            .map(jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA)).blockingFirst();
                    request(TeambrellaUris.getNewPostUri(mTopicId, uri.getQueryParameter(TeambrellaUris.KEY_ID), null, array.toString()));
                    StatisticHelper.onChatMessage(this, mTeamId, mTopicId, StatisticHelper.MESSAGE_IMAGE);
                    break;
                case TeambrellaUris.NEW_POST:
                case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                    getPager(DATA_FRAGMENT_TAG).loadNext(true);
                    break;
                case TeambrellaUris.MUTE:
                    Observable.fromArray(response.getValue())
                            .map(JsonWrapper::new)
                            .map(jsonWrapper -> jsonWrapper.getBoolean(TeambrellaModel.ATTR_DATA, false))
                            .doOnNext(isMuted -> {
                                mMuteStatus = isMuted ? MuteStatus.MUTED : MuteStatus.UMMUTED;
                                invalidateOptionsMenu();
                            }).blockingFirst();
                    break;
            }
        }
    }


    @SuppressLint("CheckResult")
    @SuppressWarnings("ThrowableNotThrown")
    private void onDataUpdated(Notification<JsonObject> response) {
        if (response.isOnNext()) {
            if (mTopicId != null) {
                mNotificationManager.cancelChatNotification(mTopicId);
            }
            Observable.fromArray(response.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .doOnNext(data -> {
                        JsonWrapper voting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
                        if (voting != null) {
                            mVote = voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, mVote);
                        }

                        JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
                        if (basic != null) {
                            if (mAction != null && mAction.equals(SHOW_CLAIM_CHAT_ACTION)) {
                                String incidentDate = basic.getString(TeambrellaModel.ATTR_DATA_INCIDENT_DATE);
                                if (incidentDate != null) {
                                    setClaimTitle(incidentDate);
                                }
                            }
                        }
                    })
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION))
                    .doOnNext(jsonWrapper -> {
                        if (jsonWrapper.hasValue(TeambrellaModel.ATTR_DATA_IS_MUTED)) {
                            if (jsonWrapper.getBoolean(TeambrellaModel.ATTR_DATA_IS_MUTED, false)) {
                                mMuteStatus = MuteStatus.MUTED;
                            } else {
                                if (mAction != null && !mAction.equals(SHOW_CONVERSATION_CHAT)) {
                                    if (mMuteStatus == MuteStatus.DEFAULT) {
                                        showNotificationHelp();
                                    }
                                }
                                mMuteStatus = MuteStatus.UMMUTED;
                            }
                        } else {
                            if (mMuteStatus == null) {
                                mMuteStatus = MuteStatus.DEFAULT;
                            }
                        }

                        mTopicId = jsonWrapper.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID, mTopicId);

                        invalidateOptionsMenu();

                        long lastRead = jsonWrapper.getLong(TeambrellaModel.ATTR_DATA_LAST_READ, -1L);
                        if (lastRead > mLastRead) {
                            if (mAction != null) {
                                switch (mAction) {
                                    case SHOW_CONVERSATION_CHAT:
                                        mChatBroadCastManager.notifyPrivateMessageRead(mUserId);
                                        mNotificationManager.cancelPrivateChatNotification(mUserId);
                                        break;
                                    default:
                                        mChatBroadCastManager.notifyTopicRead(mTopicId);
                                }
                            }
                        }
                        mLastRead = lastRead;


                    }).blockingFirst();
        } else {
            Throwable error = response.getError();

            @StringRes final int message;

            if (error instanceof TeambrellaClientException) {
                Throwable cause = error.getCause();
                message = cause instanceof SocketTimeoutException
                        || cause instanceof UnknownHostException ? R.string.no_internet_connection : R.string.something_went_wrong_error;
            } else {
                message = R.string.something_went_wrong_error;
            }

            Snackbar snackbar = Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, v -> getPager(DATA_FRAGMENT_TAG).reload())
                    .setActionTextColor(getResources().getColor(R.color.lightGold));

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    ((CoordinatorLayout.LayoutParams) sb.getView().getLayoutParams()).setBehavior(null);
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mClient != null) {
            mClient.onPause();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mClient != null) {
            mClient.onResume();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mute:
            case R.id.unmute:
                showNotificationSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Observable<ImagePicker.ImageDescriptor> result = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            result.subscribe(descriptor -> {
                String uuid = UUID.randomUUID().toString();
                ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel.class).addPendingImage(uuid, descriptor.file.getAbsolutePath(), descriptor.ratio);
                request(TeambrellaUris.getNewFileUri(descriptor.file.getAbsolutePath(), uuid));
            }, throwable -> {

            });
        }
        getPager(DATA_FRAGMENT_TAG).loadNext(true);
        if (mAction != null) {
            switch (mAction) {
                case SHOW_CLAIM_CHAT_ACTION:
                    load(CLAIM_DATA_TAG);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_LAST_READ, mLastRead);
    }

    @Override
    public int getTeamId() {
        return mTeamId;
    }

    @NonNull
    @Override
    protected String[] getDataTags() {
        if (mAction != null) {
            switch (mAction) {
                case SHOW_CLAIM_CHAT_ACTION:
                    return new String[]{CLAIM_DATA_TAG, VOTE_DATA_TAG};
            }
        }
        return new String[]{};
    }

    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return new String[]{DATA_FRAGMENT_TAG};
    }


    @Nullable
    @Override
    protected Bundle getDataConfig(@NotNull String tag) {
        switch (tag) {
            case CLAIM_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getClaimUri(getIntent().getIntExtra(EXTRA_CLAIM_ID, -1)));
            case VOTE_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getDataConfig();
        }
        return super.getDataConfig(tag);
    }

    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case DATA_FRAGMENT_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(mUri);
        }
        return super.getDataPagerConfig(tag);
    }

    @Nullable
    @Override
    protected <T extends TeambrellaPagerViewModel> Class<T> getPagerViewModelClass(@NotNull String tag) {
        switch (tag) {
            case DATA_FRAGMENT_TAG:
                //noinspection unchecked
                return (Class<T>) ChatViewModel.class;
        }
        return super.getPagerViewModelClass(tag);
    }

    @Override
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }

    @Override
    protected boolean isRequestable() {
        return true;
    }

    private void showNotificationHelp() {
        mNotificationHelpView.setVisibility(View.VISIBLE);
        mNotificationHelpView.postDelayed(() -> mNotificationHelpView.setVisibility(View.GONE), 5000);
    }

    @Override
    public int getClaimId() {
        return getIntent().getIntExtra(EXTRA_CLAIM_ID, 0);
    }

    @Override
    public String getObjectName() {
        return getIntent().getStringExtra(EXTRA_OBJECT_NAME);
    }

    @Override
    public String getUserId() {
        return getIntent().getStringExtra(EXTRA_USER_ID);
    }

    @Override
    public String getUserName() {
        return getIntent().getStringExtra(EXTRA_USER_NAME);
    }

    @Override
    public String getImageUri() {
        return getIntent().getStringExtra(EXTRA_IMAGE_URI);
    }


    private void showNotificationSettings() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(NOTIFICATION_SETTINGS_FRAGMENT_TAG) == null) {
            NotificationsSettingsDialogFragment.getInstance().show(fragmentManager, NOTIFICATION_SETTINGS_FRAGMENT_TAG);
        }
    }

    private class ChatNotificationClient extends TeambrellaNotificationServiceClient {

        private boolean mResumed;
        private boolean mReloadOnResume;

        ChatNotificationClient(Context context) {
            super(context);
        }


        @Override
        public boolean onPushMessage(INotificationMessage message) {

            final String messageTopicId = message.getTopicId();
            final String messageUserId = message.getSenderUserId();

            switch (message.getCmd()) {
                case CREATED_POST:
                    switch (mAction) {
                        case SHOW_CLAIM_CHAT_ACTION:
                        case SHOW_FEED_CHAT_ACTION:
                        case SHOW_TEAMMATE_CHAT_ACTION:
                            if (messageTopicId != null && messageTopicId.equals(mTopicId)
                                    && (messageUserId != null && !messageUserId.equals(TeambrellaUser.get(ChatActivity.this).getUserId()))) {
                                if (mResumed) {
                                    getPager(DATA_FRAGMENT_TAG).loadNext(true);
                                } else {
                                    mReloadOnResume = true;
                                }
                                return mResumed;
                            }
                    }
                    break;

                case PRIVATE_MSG:
                    if (mAction.equals(SHOW_CONVERSATION_CHAT) && messageUserId != null && messageUserId.equals(mUserId)) {
                        if (mResumed) {
                            getPager(DATA_FRAGMENT_TAG).loadNext(true);
                        } else {
                            mReloadOnResume = true;
                        }
                        return mResumed;
                    }
                    break;

                case TOPIC_MESSAGE_NOTIFICATION:
                    if (messageTopicId != null && messageTopicId.equals(mTopicId)) {
                        if (!mResumed) {
                            mReloadOnResume = true;
                        }
                    }
                    return mResumed && messageTopicId != null && messageTopicId.equals(mTopicId);
            }

            return false;
        }

        private void onResume() {
            mResumed = true;
            if (mReloadOnResume) {
                getPager(DATA_FRAGMENT_TAG).loadNext(true);
                mReloadOnResume = false;
            }
        }

        private void onPause() {
            mResumed = false;

        }
    }

    @Override
    public MuteStatus getMuteStatus() {
        return mMuteStatus;
    }

    @Override
    public void setChatMuted(boolean muted) {
        request(TeambrellaUris.getSetChatMuted(mTopicId, muted));
    }


    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void setSubtitle(String subtitle) {
        // nothing to do
    }

    @Override
    public void postVote(int vote) {
        ViewModelProviders.of(this).get(VOTE_DATA_TAG, TeambrellaDataViewModel.class)
                .load((TeambrellaUris.getClaimVoteUri(getIntent().getIntExtra(EXTRA_CLAIM_ID, -1), vote)));
        new TeambrellaBroadcastManager(this).notifyClaimVote(getClaimId());
    }

    @Override
    public void showSnackBar(int text) {
        // nothing to do
    }

    @Override
    public void launchActivity(Intent intent) {
        // nothing to do
    }

    private void setClaimTitle(@NotNull String incidentDate) {
        Date date = TeambrellaDateUtils.getDate(incidentDate);
        Date current = new Date();
        boolean isTheSameYear = date != null && date.getYear() == current.getYear();
        setTitle(getString(R.string.claim_title_date_format_string, TeambrellaDateUtils.getDatePresentation(this
                , isTheSameYear ? TeambrellaDateUtils.TEAMBRELLA_UI_DATE_CHAT_SHORT : TeambrellaDateUtils.TEAMBRELLA_UI_DATE
                , incidentDate)));
    }
}
