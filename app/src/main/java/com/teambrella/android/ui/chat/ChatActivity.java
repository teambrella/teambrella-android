package com.teambrella.android.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.data.base.TeambrellaRequestFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.ImagePicker;

import java.io.File;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Claim chat
 */
public class ChatActivity extends ADataHostActivity implements IChatActivity {

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
    private static final String DATA_REQUEST_FRAGMENT_TAG = "data_request";

    private static final String SHOW_TEAMMATE_CHAT_ACTION = "show_teammate_chat_action";
    private static final String SHOW_CLAIM_CHAT_ACTION = "show_claim_chat_action";
    private static final String SHOW_FEED_CHAT_ACTION = "show_feed_chat_action";
    private static final String SHOW_CONVERSATION_CHAT = "show_conversation_chat_action";


    private Uri mUri;
    private String mTopicId;
    private String mAction;
    private String mUserId;
    private String mUserName;
    private Uri mImageUri;
    private int mTeamId;
    private int mClaimId;
    private String mObjectName;


    private Disposable mRequestDisposable;
    private Disposable mChatDisposable;
    private TextView mMessageView;
    private ImagePicker mImagePicker;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mIcon;
    private Picasso mPicasso;
    private ChatNotificationClient mClient;

    public static void startTeammateChat(Context context, int teamId, String userId, String userName, Uri imageUri, String topicId, int accessLevel) {
        context.startActivity(getTeammateChat(context, teamId, userId, userName, imageUri, topicId, accessLevel));
    }


    public static void startClaimChat(Context context, int teamId, int claimId, String objectName, Uri imageUri, String topicId, int accessLevel) {
        context.startActivity(getClaimChat(context, teamId, claimId, objectName, imageUri, topicId, accessLevel));
    }

    public static void startFeedChat(Context context, String title, String topicId, int accessLevel) {
        context.startActivity(getFeedChat(context, title, topicId, accessLevel));
    }


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


    public static Intent getFeedChat(Context context, String title, String topicId, int accessLevel) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_TOPIC_ID, topicId)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URI, TeambrellaUris.getFeedChatUri(topicId))
                .putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
                .setAction(SHOW_FEED_CHAT_ACTION);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();

        mUri = intent.getParcelableExtra(EXTRA_URI);
        mTopicId = intent.getStringExtra(EXTRA_TOPIC_ID);
        mUserId = intent.getStringExtra(EXTRA_USER_ID);
        mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, 0);
        mUserName = intent.getStringExtra(EXTRA_USER_NAME);
        mImageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        mClaimId = intent.getIntExtra(EXTRA_CLAIM_ID, 0);
        mObjectName = intent.getStringExtra(EXTRA_OBJECT_NAME);
        mAction = intent.getAction();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_chat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
            if (mAction != null && !mAction.equals(SHOW_FEED_CHAT_ACTION)) {
                actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setCustomView(R.layout.chat_toolbar_view);
                View view = actionBar.getCustomView();
                mTitle = view.findViewById(R.id.title);
                mSubtitle = view.findViewById(R.id.subtitle);
                mIcon = view.findViewById(R.id.icon);
                Toolbar parent = (Toolbar) view.getParent();
                parent.setPadding(0, 0, 0, 0);
                parent.setContentInsetsAbsolute(0, 0);
            } else {
                setTitle(intent.getStringExtra(EXTRA_TITLE));
            }
        }

        mImagePicker = new ImagePicker(this);
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT_TAG) == null) {
            transaction.add(R.id.container, ChatFragment.getInstance(DATA_FRAGMENT_TAG, ChatFragment.class), UI_FRAGMENT_TAG);
        }


        if (fragmentManager.findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG) == null) {
            transaction.add(new TeambrellaRequestFragment(), DATA_REQUEST_FRAGMENT_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }


        mMessageView = findViewById(R.id.text);
        findViewById(R.id.send_text).setOnClickListener(this::onClick);
        findViewById(R.id.send_image).setOnClickListener(this::onClick);


        mPicasso = TeambrellaImageLoader.getInstance(this).getPicasso();

        if (mAction != null) {
            switch (mAction) {
                case SHOW_TEAMMATE_CHAT_ACTION:

                    if (mTitle != null) {
                        mTitle.setText(R.string.application);
                    }

                    if (mSubtitle != null) {
                        mSubtitle.setText(mUserName);
                    }

                    if (mImageUri != null && mIcon != null) {
                        mPicasso.load(mImageUri)
                                .transform(new CropCircleTransformation())
                                .into(mIcon);

                        mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, mUserName, mImageUri.toString()));
                    }

                    break;

                case SHOW_CLAIM_CHAT_ACTION:

                    if (mTitle != null) {
                        mTitle.setText(getString(R.string.claim_title_format_string, mClaimId));
                    }

                    if (mSubtitle != null) {
                        mSubtitle.setText(mObjectName);
                    }


                    if (mImageUri != null && mIcon != null) {
                        mPicasso.load(mImageUri)
                                .resizeDimen(R.dimen.image_size_40, R.dimen.image_size_40)
                                .centerCrop()
                                .transform(new MaskTransformation(this, R.drawable.teammate_object_mask))
                                .into(mIcon);

                        mIcon.setOnClickListener(v -> {
                            ClaimActivity.start(this, mClaimId, mObjectName, mTeamId);
                            overridePendingTransition(0, 0);
                        });
                    }

                    break;

                case SHOW_CONVERSATION_CHAT:

                    if (mTitle != null) {
                        mTitle.setText(R.string.private_conversation);
                    }

                    if (mSubtitle != null) {
                        mSubtitle.setText(intent.getStringExtra(EXTRA_USER_NAME));
                    }

                    if (mImageUri != null && mIcon != null) {
                        mPicasso.load(mImageUri)
                                .transform(new CropCircleTransformation())
                                .into(mIcon);

                        mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, mUserName, mImageUri.toString()));
                    }


                    findViewById(R.id.send_image).setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMessageView.getLayoutParams();
                    params.leftMargin = getResources().getDimensionPixelSize(R.dimen.margin_8);
                    mMessageView.setLayoutParams(params);

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
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            mRequestDisposable = fragment.getObservable().subscribe(this::onRequestResult);
            fragment.start();
        }

        mChatDisposable = getPager(DATA_FRAGMENT_TAG).getObservable()
                .subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.stop();
        }
        if (mRequestDisposable != null && !mRequestDisposable.isDisposed()) {
            mRequestDisposable.dispose();
        }

        mRequestDisposable = null;


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

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_text:

                String text = mMessageView.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    switch (mAction) {
                        case SHOW_CONVERSATION_CHAT:
                            request(TeambrellaUris.getNewConversationMessage(mUserId, mMessageView.getText().toString()));
                            break;
                        default:
                            request(TeambrellaUris.getNewPostUri(mTopicId, mMessageView.getText().toString(), null));
                    }
                }
                mMessageView.setText(null);
                break;
            case R.id.send_image:
                mImagePicker.startPicking();
                break;
        }
    }


    public void request(Uri uri) {
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.request(uri);
        }
    }


    @Override
    public Uri getChatUri() {
        return getIntent().getParcelableExtra(EXTRA_URI);
    }

    private void onRequestResult(Notification<JsonObject> response) {
        if (response.isOnNext()) {
            String requestUriString = Observable.just(response.getValue()).map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI))
                    .blockingFirst(null);
            switch (TeambrellaUris.sUriMatcher.match(Uri.parse(requestUriString))) {
                case TeambrellaUris.NEW_FILE:
                    JsonArray array = Observable.just(response.getValue()).map(JsonWrapper::new)
                            .map(jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA)).blockingFirst();
                    request(TeambrellaUris.getNewPostUri(mTopicId, null, array.toString()));
                    break;
                case TeambrellaUris.NEW_POST:
                case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                    getPager(DATA_FRAGMENT_TAG).loadNext(true);
                    break;
            }
        }
    }


    private void onDataUpdated(Notification<JsonObject> response) {
        if (response.isOnNext()) {
            Observable.just(response.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                    .map(Notification::createOnNext)
                    .onErrorReturn(Notification::createOnError)
                    .doOnNext(this::onBasicPartUpdated)
                    .blockingFirst();
        }
    }


    private void onBasicPartUpdated(Notification<JsonWrapper> basicNotification) {
        if (basicNotification.isOnNext()) {
            JsonWrapper basic = basicNotification.getValue();
            if (mAction != null) {
                switch (mAction) {
                    case SHOW_TEAMMATE_CHAT_ACTION:
                        mUserName = basic.getString(TeambrellaModel.ATTR_DATA_NAME);
                        if (mImageUri == null) {
                            mImageUri = TeambrellaImageLoader.getImageUri(basic.getString(TeambrellaModel.ATTR_DATA_AVATAR));
                            if (mImageUri != null) {
                                mPicasso.load(mImageUri)
                                        .transform(new CropCircleTransformation())
                                        .into(mIcon);
                            }
                        }

                        mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, mUserName, mImageUri.toString()));

                        mSubtitle.setText(mUserName);
                        break;
                }
            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Observable<File> result = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            result.subscribe(file -> request(TeambrellaUris.getNewFileUri(file.getAbsolutePath())),
                    throwable -> {
                    });
        }
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


    private class ChatNotificationClient extends TeambrellaNotificationServiceClient {

        ChatNotificationClient(Context context) {
            super(context);
        }


        @Override
        public boolean onPrivateMessage(String userId, String name, String avatar, String text) {
            if (mAction.equals(SHOW_CONVERSATION_CHAT)
                    && userId.equals(mUserId)) {
                getPager(DATA_FRAGMENT_TAG).loadNext(true);
                return true;
            }

            return false;
        }

        @Override
        public boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {
            switch (mAction) {
                case SHOW_CLAIM_CHAT_ACTION:
                case SHOW_FEED_CHAT_ACTION:
                case SHOW_TEAMMATE_CHAT_ACTION:
                    if (topicId.equals(mTopicId) && userId == null || !userId.equals(TeambrellaUser.get(ChatActivity.this).getUserId())) {
                        getPager(DATA_FRAGMENT_TAG).loadNext(true);
                        return true;
                    }
            }
            return false;
        }
    }
}
