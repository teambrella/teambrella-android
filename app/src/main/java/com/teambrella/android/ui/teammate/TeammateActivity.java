package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.votes.AllVotesActivity;
import com.teambrella.android.util.StatisticHelper;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Teammate screen.
 */
public class TeammateActivity extends TeambrellaDataHostActivity implements ITeammateActivity {

    private static final String TEAMMATE_URI = "teammate_uri";
    private static final String TEAMMATE_NAME = "teammate_name";
    private static final String TEAMMATE_PICTURE = "teammate_picture";
    private static final String TEAMMATE_USER_ID = "teammate_user_id";
    private static final String CURRENCY = "currency";
    private static final String TEAM_ID = "team_id";

    private static final String DATA_FRAGMENT = "data";
    private static final String VOTE_FRAGMENT = "vote";
    private static final String PROXY_FRAGMENT = "proxy";
    private static final String UI_FRAGMENT = "ui";


    private static final int DEFAULT_REQUEST_CODE = 3;


    private Disposable mDisposal;
    private int mTeammateId = -1;
    private String mUserId = null;
    private String mUserName = null;
    private int mTeamId;
    private String mTopicId;
    private Uri mAvatar = null;
    private String mCurrency;
    private Snackbar mSnackBar;
    private TextView mTitleView;

    private TeammateNotificationClient mNotificationClient;


    public static Intent getIntent(Context context, int teamId, String userId, String name, String userPictureUri) {
        return new Intent(context, TeammateActivity.class)
                .putExtra(TEAMMATE_USER_ID, userId)
                .putExtra(TEAMMATE_URI, TeambrellaUris.getTeammateUri(teamId, userId))
                .putExtra(TEAMMATE_NAME, name)
                .putExtra(TEAM_ID, teamId)
                .putExtra(TEAMMATE_PICTURE, userPictureUri);
    }

    public static void start(Context context, int teamId, String userId, String name, String userPictureUri) {
        context.startActivity(getIntent(context, teamId, userId, name, userPictureUri));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mCurrency = getIntent().getStringExtra(CURRENCY);
        mUserId = getIntent().getStringExtra(TEAMMATE_USER_ID);
        mTeamId = getIntent().getIntExtra(TEAM_ID, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_teammate);


        mNotificationClient = new TeammateNotificationClient(this);
        mNotificationClient.connect();


        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ADataProgressFragment.getInstance(new String[]{DATA_FRAGMENT, VOTE_FRAGMENT, PROXY_FRAGMENT}, TeammateFragment.class), UI_FRAGMENT)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.teammate_toolbar_view);
            View customView = actionBar.getCustomView();
            Toolbar parent = (Toolbar) customView.getParent();
            parent.setPadding(0, 0, 0, 0);
            parent.setContentInsetsAbsolute(0, 0);
            mTitleView = customView.findViewById(R.id.title);


            View sendMessageView = customView.findViewById(R.id.send_message);
            if (TeambrellaUser.get(this).getUserId().equals(mUserId)) {
                sendMessageView.setVisibility(View.GONE);
            } else {
                customView.findViewById(R.id.send_message).setOnClickListener(v ->
                        ChatActivity.startConversationChat(this, mUserId, mUserName, mAvatar));
            }

        }
        setTitle(getIntent().getStringExtra(TEAMMATE_NAME));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDisposal = getObservable(DATA_FRAGMENT).subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationClient != null) {
            mNotificationClient.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mNotificationClient != null) {
            mNotificationClient.onPause();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.votes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.votes:
                AllVotesActivity.startTeammateAllVotes(this, mTeamId, mTeammateId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }


    @Override
    public void postVote(double vote) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(VOTE_FRAGMENT);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getTeammateVoteUri(mTeammateId, vote));
        }

        StatisticHelper.onApplicationVote(getTeamId(), vote);
    }

    @Override
    public void setAsProxy(boolean set) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(VOTE_FRAGMENT);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.setMyProxyUri(mUserId, set));
        }
    }


    @Override
    public boolean isItMe() {
        return mUserId != null && mUserId.equals(TeambrellaUser.get(this).getUserId());
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{DATA_FRAGMENT, VOTE_FRAGMENT, PROXY_FRAGMENT};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{};
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        return null;
    }


    @Override
    public String getCurrency() {
        return mCurrency;
    }


    @Override
    public int getTeamId() {
        return mTeamId;
    }

    @Override
    public int getTeammateId() {
        return mTeammateId;
    }

    @Override
    public void showSnackBar(@StringRes int text) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG);

            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    mSnackBar = null;
                }
            });

            mSnackBar.show();
        }
    }


    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT:
                return TeambrellaDataFragment
                        .getInstance(getIntent().getParcelableExtra(TEAMMATE_URI));
            case VOTE_FRAGMENT:
            case PROXY_FRAGMENT:
                return TeambrellaDataFragment.getInstance(null);
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        load(DATA_FRAGMENT);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Observable.fromArray(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA))
                    .doOnNext(node -> mTeammateId = node.getInt(TeambrellaModel.ATTR_DATA_ID))
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                    .doOnNext(node -> mUserId = node.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                    .doOnNext(node -> mUserName = node.getString(TeambrellaModel.ATTR_DATA_NAME))
                    .doOnNext(node -> mAvatar = TeambrellaImageLoader.getImageUri(node.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .onErrorReturnItem(new JsonWrapper(null))
                    .blockingFirst();

            Observable.fromArray(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA))
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION))
                    .doOnNext(node -> mTopicId = node.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID))
                    .onErrorReturnItem(new JsonWrapper(null))
                    .blockingFirst();


        }
    }


    @Override
    public void launchActivity(Intent intent) {
        startActivityForResult(intent, DEFAULT_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNotificationClient != null) {
            mNotificationClient.disconnect();
            mNotificationClient = null;
        }
    }

    private class TeammateNotificationClient extends TeambrellaNotificationServiceClient {

        private boolean mResumed;
        private boolean mUpdateOnResume;

        TeammateNotificationClient(Context context) {
            super(context);
        }


        @Override
        public boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {
            if (topicId != null && topicId.equals(mTopicId)) {
                if (mResumed) {
                    load(DATA_FRAGMENT);
                } else {
                    mUpdateOnResume = true;
                }
            }
            return false;
        }

        void onPause() {
            mResumed = false;
        }

        void onResume() {
            mResumed = true;
            if (mUpdateOnResume) {
                load(DATA_FRAGMENT);
                mUpdateOnResume = false;
            }
        }
    }
}
