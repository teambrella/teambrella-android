package com.teambrella.android.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.services.TeambrellaNotificationManager;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.ImagePicker;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.UUID;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Claim chat
 */
public class ChatActivity extends ATeambrellaActivity implements IChatActivity {

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


    private static final String DATA_FRAGMENT_TAG = "data_fragment_tag";
    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";
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

    private Disposable mChatDisposable;
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

    public static void startConversationChat(Context context, String userId, String userName, Uri imageUri) {
        context.startActivity(getConversationChat(context, userId, userName, imageUri));
    }

    public static Intent getConversationChat(Context context, String userId, String userName, Uri imageUri) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_USER_ID, userId)
                .putExtra(EXTRA_URI, TeambrellaUris.getConversationChat(userId))
                .putExtra(EXTRA_USER_NAME, userName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .setAction(SHOW_CONVERSATION_CHAT);
    }

    public static Intent getClaimChat(Context context, int teamId, int claimId, String objectName, Uri imageUri, String topicId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_CLAIM_ID, claimId)
                .putExtra(EXTRA_OBJECT_NAME, objectName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_URI, TeambrellaUris.getClaimChatUri(claimId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .setAction(SHOW_CLAIM_CHAT_ACTION);
    }

    public static Intent getTeammateChat(Context context, int teamId, String userId, String userName, Uri imageUri, String topicId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_USER_ID, userId)
                .putExtra(EXTRA_USER_NAME, userName)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_URI, TeambrellaUris.getTeammateChatUri(teamId, userId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .setAction(SHOW_TEAMMATE_CHAT_ACTION);
    }


    public static Intent getFeedChat(Context context, String title, String topicId, int teamId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URI, TeambrellaUris.getFeedChatUri(topicId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .setAction(SHOW_FEED_CHAT_ACTION);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();

        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_window_background));

        mUri = intent.getParcelableExtra(EXTRA_URI);
        mTopicId = intent.getStringExtra(EXTRA_TOPIC_ID);
        mUserId = intent.getStringExtra(EXTRA_USER_ID);
        mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
        int mClaimId = intent.getIntExtra(EXTRA_CLAIM_ID, 0);
        mAction = intent.getAction();

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
            transaction.add(R.id.container, ChatFragment.getInstance(DATA_FRAGMENT_TAG, ChatFragment.class), UI_FRAGMENT_TAG);
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

                case SHOW_CLAIM_CHAT_ACTION:
                    setTitle(getString(R.string.claim_title_format_string, mClaimId));
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

                    Uri mImageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
                    if (mImageUri != null && mIcon != null) {
                        Glide.with(this).load(mImageUri)
                                .apply(new RequestOptions().transforms(new CenterCrop(), new CircleCrop()))
                                .into(mIcon);
                        mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, intent.getStringExtra(EXTRA_USER_NAME), mImageUri.toString()));
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
    protected void onStart() {
        super.onStart();
        mChatDisposable = getPager(DATA_FRAGMENT_TAG).getObservable()
                .subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mChatDisposable != null && !mChatDisposable.isDisposed()) {
            mChatDisposable.dispose();
        }

        mChatDisposable = null;
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
                    switch (mAction) {
                        case SHOW_CONVERSATION_CHAT: {
                            String uuid = UUID.randomUUID().toString();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            ChatPagerFragment fragment = (ChatPagerFragment) fragmentManager.findFragmentByTag(DATA_FRAGMENT_TAG);
                            fragment.addPendingMessage(uuid, text, -1f);
                            request(TeambrellaUris.getNewConversationMessage(mUserId, uuid, text));
                        }
                        break;
                        default: {
                            String uuid = UUID.randomUUID().toString();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            ChatPagerFragment fragment = (ChatPagerFragment) fragmentManager.findFragmentByTag(DATA_FRAGMENT_TAG);
                            fragment.addPendingMessage(uuid, text, mVote);
                            request(TeambrellaUris.getNewPostUri(mTopicId, uuid, text, null));
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
                        invalidateOptionsMenu();
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
                finish();
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
                FragmentManager fragmentManager = getSupportFragmentManager();
                ChatPagerFragment fragment = (ChatPagerFragment) fragmentManager.findFragmentByTag(DATA_FRAGMENT_TAG);
                fragment.addPendingImage(uuid, descriptor.file.getAbsolutePath(), descriptor.ratio);
                request(TeambrellaUris.getNewFileUri(descriptor.file.getAbsolutePath(), uuid));
            }, throwable -> {

            });
        }
        getPager(DATA_FRAGMENT_TAG).loadNext(true);
    }

    @Override
    public int getTeamId() {
        return mTeamId;
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{DATA_FRAGMENT_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }


    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT_TAG:
                return ChatPagerFragment.getInstance(mUri, null, ChatPagerFragment.class);
        }
        return null;
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
        public boolean onPrivateMessage(String userId, String name, String avatar, String text) {
            if (mAction.equals(SHOW_CONVERSATION_CHAT)
                    && userId.equals(mUserId)) {
                if (mResumed) {
                    getPager(DATA_FRAGMENT_TAG).loadNext(true);
                } else {
                    mReloadOnResume = true;
                }
                return mResumed;
            }

            return false;
        }

        @Override
        public boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {
            switch (mAction) {
                case SHOW_CLAIM_CHAT_ACTION:
                case SHOW_FEED_CHAT_ACTION:
                case SHOW_TEAMMATE_CHAT_ACTION:
                    if (topicId.equals(mTopicId) && (userId != null && !userId.equals(TeambrellaUser.get(ChatActivity.this).getUserId()))) {
                        if (mResumed) {
                            getPager(DATA_FRAGMENT_TAG).loadNext(true);
                        } else {
                            mReloadOnResume = true;
                        }
                        return mResumed;
                    }
            }
            return false;
        }

        @Override
        public boolean onChatNotification(String topicId) {
            if (topicId.equals(mTopicId)) {
                if (!mResumed) {
                    mReloadOnResume = true;
                }
            }
            return mResumed && topicId.equals(mTopicId);
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
}
