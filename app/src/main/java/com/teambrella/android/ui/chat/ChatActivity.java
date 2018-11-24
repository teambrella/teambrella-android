package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.TeambrellaApplication;
import com.teambrella.android.api.TeambrellaClientException;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataLoaderKt;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.services.TeambrellaNotificationManager;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivityKt;
import com.teambrella.android.ui.base.TeambrellaBroadcastManager;
import com.teambrella.android.ui.base.TeambrellaDataViewModel;
import com.teambrella.android.ui.base.TeambrellaPagerViewModel;
import com.teambrella.android.ui.claim.IClaimActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.ImagePicker;
import com.teambrella.android.util.StatisticHelper;
import com.teambrella.android.util.TeambrellaDateUtils;
import com.teambrella.android.util.log.Log;

import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ArrayList;
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

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

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
    private static final String PIN_UNPIN_DATA = "pin_unpin_data_fragment";
    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";
    public static final String CLAIM_DATA_TAG = "claim_data_tag";
    public static final String VOTE_DATA_TAG = "vote_data_tag";
    private static final String NOTIFICATION_SETTINGS_FRAGMENT_TAG = "notification_settings";
    private static final String PIN_UNPIN_FRAGMENT_TAG = "pin_unpin";

    private static final String SHOW_TEAMMATE_CHAT_ACTION = "show_teammate_chat_action";
    private static final String SHOW_CLAIM_CHAT_ACTION = "show_claim_chat_action";
    private static final String SHOW_FEED_CHAT_ACTION = "show_feed_chat_action";
    private static final String SHOW_CONVERSATION_CHAT = "show_conversation_chat_action";

    private Uri mUri;
    private String mTopicId;
    private String mAction;
    private String mUserId;
    private int mTeamId;
    private boolean restoredUris;

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
    private boolean mFullAccess = false;

    private View mContainer;
    private Snackbar mSnackBar;
    private Uri requestInProcess;
    private LinkedHashSet<Uri> urisToProcess = new LinkedHashSet<>();

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
        findViewById(R.id.add_photos).setOnClickListener(this::onClick);

        Boolean needHideSendImage = false;
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
                    needHideSendImage = true;
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
                mFullAccess = true;
                break;
            default:
                //needHideSendImage = true;
                if (mUserId != null && mUserId.equals(TeambrellaUser.get(this).getUserId())) {
                    findViewById(R.id.input).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.input).setVisibility(View.GONE);
                }
                break;
        }

        if (needHideSendImage) {
            findViewById(R.id.send_image).setVisibility(View.GONE);
            findViewById(R.id.add_photos).setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMessageView.getLayoutParams();
            params.leftMargin = getResources().getDimensionPixelSize(R.dimen.margin_8);
            mMessageView.setLayoutParams(params);
        }
        else {
            findViewById(R.id.send_text).setVisibility(View.GONE);
            mMessageView.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    String text = mMessageView.getText().toString().trim();
                    Log.v(LOG_TAG, "[anim] A(" + text + ")");
                    if (text.length() > 0) {
                        setAnimation(false, findViewById(R.id.add_photos));
                        setAnimation(true, findViewById(R.id.send_text));
                    } else {
                        setAnimation(true, findViewById(R.id.add_photos));
                        setAnimation(false, findViewById(R.id.send_text));
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    String text = mMessageView.getText().toString().trim();
                    Log.v(LOG_TAG, "[anim] B(" + text + ")");
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = mMessageView.getText().toString().trim();
                    Log.v(LOG_TAG, "[anim] C(" + text + ")");
                }
            });
        }

        mClient = new ChatNotificationClient(this);
        mClient.connect();

        setResult(RESULT_OK);
    }

    protected void setAnimation(boolean fadeIn, View view) {
        Animation anim = new AlphaAnimation(fadeIn?0:1, fadeIn?1:0);
        anim.setInterpolator(fadeIn ? new DecelerateInterpolator() : new AccelerateInterpolator());
        anim.setDuration(100);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (fadeIn) {
                    Log.v(LOG_TAG, "[anim] setAnimation: onAnimationStart(fadeIn) " + view.toString());
                    view.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!fadeIn) {
                    Log.v(LOG_TAG, "[anim] setAnimation: onAnimationStart(fadeOut) " + view.toString());
                    view.setVisibility(View.GONE);
                }
                view.setAnimation(null);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        if (view.getVisibility() == View.GONE && fadeIn
                || view.getVisibility() == View.VISIBLE && !fadeIn) {
            Log.v(LOG_TAG, "[anim] setAnimation: setAnimation() " + view.toString());
            view.startAnimation(anim);
        }
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

                if (mFullAccess) {
                    menu.add(0, R.id.pin, 0, null)
                            .setIcon(R.drawable.ic_pin_grey).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }

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
                            queuedRequest(TeambrellaUris.getNewConversationMessage(mUserId, uuid, text));
                            StatisticHelper.onPrivateMessage(this);
                        }
                        break;
                        default: {
                            String uuid = UUID.randomUUID().toString();
                            model.addPendingMessage(uuid, text, mVote);
                            queuedRequest(TeambrellaUris.getNewPostUri(mTopicId, uuid, text, null));
                            StatisticHelper.onChatMessage(this, mTeamId, mTopicId, StatisticHelper.MESSAGE_TEXT);
                        }
                    }
                }
                mMessageView.setText(null);
                break;
            case R.id.send_image:
                startImagePicking();
                break;
            case R.id.add_photos:
                startTakingPhoto();
                break;
        }
    }

    public void startImagePicking() {
        mImagePicker.startPicking(getString(R.string.choose));
    }
    public void startTakingPhoto() {
        mImagePicker.startTakingPhoto(getString(R.string.choose));
    }

    @Override
    public Uri getChatUri() {
        return getIntent().getParcelableExtra(EXTRA_URI);
    }

    protected Uri getRequestUri(Notification<JsonObject> response) {
        String requestUriString = Observable.just(response.getValue()).map(JsonWrapper::new)
                .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                .map(jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI))
                .blockingFirst(null);
        return Uri.parse(requestUriString);
    }

    public synchronized void restoreUrisToProcess() {
        LinkedHashSet<Uri> storedUris = TeambrellaUser.get(this).getPendingUris();
        for(Uri uri : storedUris) {
            queuedRequest(uri);
        }
    }

    public synchronized void queuedRequest(Uri uri) {
        urisToProcess.add(uri);
        TeambrellaUser.get(this).setPendingUris(urisToProcess);

        if (requestInProcess == null) {
            requestInProcess = uri;
            request(uri);
        }
    }

    protected synchronized void onRequestResult(Notification<JsonObject> response) {
        if (response.isOnNext()) {

            Uri uri = getRequestUri(response);
            if (uri.equals(requestInProcess)) {
                requestInProcess = null;
            }
            Log.v(LOG_TAG, "(onRequestResult) Removing Uri: " + uri);
            urisToProcess.remove(uri);
            TeambrellaUser.get(this).setPendingUris(urisToProcess);

            // Process request
            switch (TeambrellaUris.sUriMatcher.match(uri)) {
                case TeambrellaUris.NEW_FILE:
                    JsonArray array = Observable.just(response.getValue()).map(JsonWrapper::new)
                            .map(jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA)).blockingFirst();
                    Uri uriNew = TeambrellaUris.getNewPostUri(mTopicId, uri.getQueryParameter(TeambrellaUris.KEY_ID), null, array.toString());
                    Log.v(LOG_TAG, "(onRequestResult) Adding Uri: " + uriNew);
                    urisToProcess.add(uriNew);
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


            Iterator<Uri> iter = urisToProcess.iterator();
            while (iter.hasNext()) {
                Uri storedUri = iter.next();
                // Process Files last, updates posts as they come
                if (TeambrellaUris.sUriMatcher.match(storedUri) != TeambrellaUris.NEW_FILE) {
                    Log.v(LOG_TAG, "(onRequestResult) Processing Uri-1: " + uri);
                    queuedRequest(storedUri);
                    return;
                }
            }
            if (urisToProcess.size() > 0) {
                Log.v(LOG_TAG, "(onRequestResult) Processing Uri-2: " + uri);
                queuedRequest(urisToProcess.iterator().next());
            }
        }
        else if (response.isOnError()) {
            TeambrellaException exception = (TeambrellaException) response.getError();
            final Uri uri = exception.getUri();
            if (uri.equals(requestInProcess)) {
                requestInProcess = null;
            }
            switch (TeambrellaUris.sUriMatcher.match(uri)) {
                case TeambrellaUris.NEW_FILE:
                case TeambrellaUris.NEW_POST:
                case TeambrellaUris.DELETE_POST:
                case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                    Log.v(LOG_TAG, "(onRequestResult - error) Adding Uri: " + uri);
                    urisToProcess.add(uri);
                    showSnackBar(exception);
                    break;
            }
        }
    }


    private void showSnackBar(TeambrellaException exception) {
        @StringRes final int message;

        Boolean shouldRetry = false;
        if (exception instanceof TeambrellaClientException) {
            shouldRetry = true;
            Throwable cause = exception.getCause();
            message = cause instanceof SocketTimeoutException
                    || cause instanceof UnknownHostException ? R.string.no_internet_connection : R.string.something_went_wrong_error;
        } else {
            message = R.string.something_went_wrong_error;
        }

        if (mSnackBar == null && shouldRetry) {
            final Uri uri = exception.getUri();
            mContainer = findViewById(R.id.container);
            mSnackBar = Snackbar.make(mContainer, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, v -> {
                        switch (TeambrellaUris.sUriMatcher.match(uri)) {
                            case TeambrellaUris.NEW_FILE:
                            case TeambrellaUris.NEW_POST:
                            case TeambrellaUris.DELETE_POST:
                            case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                                if (urisToProcess.size() > 0) {
                                    Log.v(LOG_TAG, "(showSnackBar) Processing Uri-1: " + uri);
                                    queuedRequest(urisToProcess.iterator().next());
                                }
                                break;
                            default:
                                IDataPager pager = getPager(DATA_FRAGMENT_TAG);
                                pager.reload(uri);
                                break;
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.lightGold));

            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    ((CoordinatorLayout.LayoutParams) sb.getView().getLayoutParams()).setBehavior(null);
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    mContainer.setTranslationY(0.0f);
                    mContainer.setPadding(0, -Math.round(0), 0, 0);
                    mSnackBar = null;
                }

            });
            mSnackBar.show();
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

            // Let chat load first
            if (!restoredUris) {
                restoredUris = true;
                restoreUrisToProcess();
            }
        } else {
            if (response.getError() instanceof TeambrellaException){
                showSnackBar((TeambrellaException)response.getError());
            }
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
            case R.id.pin:
                showPinTopicDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Observable<ImagePicker.ImageDescriptor> result = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            TeambrellaDataLoaderKt.subscribeAutoDispose(result, descriptor -> {
                String uuid = UUID.randomUUID().toString();
                ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel.class).addPendingImage(uuid, descriptor.file.getAbsolutePath(), descriptor.ratio);
                queuedRequest(TeambrellaUris.getNewFileUri(descriptor.file.getAbsolutePath(), uuid, descriptor.cameraUsed));
                return null;
            }, throwable -> {
                // SnakBar is shown in onRequestResult
                return null;
            }, () -> null);
        }
        else {
            getPager(DATA_FRAGMENT_TAG).loadNext(true);
            if (mAction != null) {
                switch (mAction) {
                    case SHOW_CLAIM_CHAT_ACTION:
                        load(CLAIM_DATA_TAG);
                }
            }
        }
    }

    public void deletePost(String postId) {
        ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel.class).deleteMyImage(postId);
        queuedRequest(TeambrellaUris.getDeletePostUri(postId));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mImagePicker.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    return new String[]{CLAIM_DATA_TAG, VOTE_DATA_TAG, PIN_UNPIN_DATA};
                case SHOW_FEED_CHAT_ACTION:
                    return new String[]{PIN_UNPIN_DATA};
                case SHOW_TEAMMATE_CHAT_ACTION:
                    return new String[]{PIN_UNPIN_DATA};
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
            case PIN_UNPIN_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getTopicPinUri(getIntent().getStringExtra(EXTRA_TOPIC_ID)), true);
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


    private void showPinTopicDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(PIN_UNPIN_FRAGMENT_TAG) == null) {
            new PinTopicDialogFragment().show(fragmentManager, PIN_UNPIN_FRAGMENT_TAG);
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


    @Override
    public LiveData<Notification<JsonObject>> getPinTopicObservable() {
        return getObservable(PIN_UNPIN_DATA);
    }

    @Override
    public void pinTopic() {
        ViewModelProviders.of(this).get(PIN_UNPIN_DATA, TeambrellaDataViewModel.class)
                .load((TeambrellaUris.getUpdateTopicUri(getIntent().getStringExtra(EXTRA_TOPIC_ID), 1)));
    }

    @Override
    public void unpinTopic() {
        ViewModelProviders.of(this).get(PIN_UNPIN_DATA, TeambrellaDataViewModel.class)
                .load((TeambrellaUris.getUpdateTopicUri(getIntent().getStringExtra(EXTRA_TOPIC_ID), -1)));
    }

    @Override
    public void resetPin() {
        ViewModelProviders.of(this).get(PIN_UNPIN_DATA, TeambrellaDataViewModel.class)
                .load((TeambrellaUris.getUpdateTopicUri(getIntent().getStringExtra(EXTRA_TOPIC_ID), 0)));
    }

    private void setClaimTitle(@NotNull String incidentDate) {
        Date date = TeambrellaDateUtils.getDate(incidentDate);
        Date current = new Date();
        boolean isTheSameYear = date != null && date.getYear() == current.getYear();
        setTitle(getString(R.string.claim_title_date_format_string, TeambrellaDateUtils.getDatePresentation(this
                , isTheSameYear ? TeambrellaDateUtils.TEAMBRELLA_UI_DATE_CHAT_SHORT : TeambrellaDateUtils.TEAMBRELLA_UI_DATE
                , incidentDate)));
    }

    public boolean isFullAccess() {
        return mFullAccess;
    }
}
