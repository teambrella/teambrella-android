package com.teambrella.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.backup.WalletBackupManager;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.chat.StartNewChatActivity;
import com.teambrella.android.ui.claim.ClaimsDataPagerFragment;
import com.teambrella.android.ui.home.HomeFragment;
import com.teambrella.android.ui.proxies.ProxiesFragment;
import com.teambrella.android.ui.team.TeamFragment;
import com.teambrella.android.ui.team.teammates.TeammatesDataPagerFragment;
import com.teambrella.android.ui.teammate.ITeammateActivity;
import com.teambrella.android.ui.user.UserFragment;
import com.teambrella.android.ui.user.wallet.WalletBackupInfoFragment;
import com.teambrella.android.util.StatisticHelper;
import com.teambrella.android.util.TeambrellaUtilService;
import com.teambrella.android.util.log.Log;

import java.util.Stack;

import io.reactivex.disposables.Disposable;


/**
 * Main Activity
 */
public class MainActivity extends ATeambrellaActivity implements IMainDataHost, ITeammateActivity {

    /**
     * Action to show feed
     */
    public static final String ACTION_SHOW_FEED = "action_show_feed";

    /**
     * Action to show wallet
     */
    public static final String ACTION_SHOW_WALLET = "action_show_wallet";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_REQUEST_CODE = 102;


    private static final String USER_ID_EXTRA = "user_id_extra";
    private static final String TEAM_EXTRA = "team_extra";

    public static final String TEAMMATES_DATA_TAG = "teammates";
    public static final String CLAIMS_DATA_TAG = "claims";
    public static final String HOME_DATA_TAG = "home_data";
    public static final String FEED_DATA_TAG = "feed_data";
    public static final String MY_PROXIES_DATA = "my_proxies_data";
    public static final String PROXIES_FOR_DATA = "proxies_for_data";
    public static final String USER_RATING_DATA = "user_rating_data";
    public static final String SET_PROXY_POSITION_DATA = "set_proxy_position";
    public static final String TEAMS_DATA = "teams_data";
    public static final String USER_DATA = "user_data";
    public static final String WALLET_DATA = "wallet_data";
    public static final String VOTE_DATA = "vote_data";


    private static final String HOME_TAG = "home";
    private static final String TEAM_TAG = "team";
    private static final String PROXIES_TAG = "proxies";
    private static final String PROFILE_TAG = "profile";
    private static final String TEAM_CHOOSER_FRAGMENT_TAG = "team_chooser";
    private static final String WALLET_BACKUP_FRAGMENT_TAG = "wallet_backup_dialog";


    private int mSelectedItemId = -1;
    private String mUserId;
    private String mUserName;
    private String mFBName;
    private Uri mUserPicture;
    private Disposable mDisposable;
    private ImageView mAvatar;
    private JsonWrapper mTeam;
    private Snackbar mSnackBar;
    private MainNotificationClient mClient;
    private EtherAccount mEtherAccount;

    private Stack<Integer> mBackStack = new Stack<>();
    private WalletBackupManager mWalletBackupManager;

    public static Intent getLaunchIntent(Context context, String userId, String team) {
        return new Intent(context, MainActivity.class)
                .putExtra(TEAM_EXTRA, team)
                .putExtra(USER_ID_EXTRA, userId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID_EXTRA);

        mTeam = intent.hasExtra(TEAM_EXTRA) ? new JsonWrapper(new Gson()
                .fromJson(intent.getStringExtra(TEAM_EXTRA)
                        , JsonObject.class)) : null;

        super.onCreate(savedInstanceState);

        if (mTeam != null) {
            setContentView(R.layout.activity_main);
            mAvatar = findViewById(R.id.avatar);
            findViewById(R.id.home).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.team).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.proxies).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.me).setOnClickListener(this::onNavigationItemSelected);
            onNavigationItemSelected(findViewById(R.id.home));
            if (savedInstanceState == null) {
                startService(new Intent(this, TeambrellaNotificationService.class).setAction(TeambrellaNotificationService.CONNECT_ACTION));
            }
            mClient = new MainNotificationClient(this);
            mClient.connect();
            onNewIntent(intent);
        } else {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class)
                    .putExtra(WelcomeActivity.CUSTOM_ACTION, intent.getAction()));
        }

        TeambrellaUtilService.scheduleWalletSync(this);
        TeambrellaUtilService.scheduleCheckingSocket(this);
        TeambrellaUtilService.oneoffWalletSync(this);
        getComponent().inject(this);
        mWalletBackupManager = new WalletBackupManager(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case ACTION_SHOW_WALLET:
                    showWallet();
                    break;
                case ACTION_SHOW_FEED:
                    showFeed();
                    break;
            }
            load(HOME_DATA_TAG);
        }

    }

    private boolean onNavigationItemSelected(View view) {
        return onNavigationItemSelected(view, true, false);
    }

    private boolean onNavigationItemSelected(View view, boolean immediately) {
        return onNavigationItemSelected(view, true, immediately);
    }


    private boolean onNavigationItemSelected(View view, boolean fromUser, boolean immediately) {

        if (mSelectedItemId == view.getId()) {
            return false;
        }

        if (mSelectedItemId > 0 && fromUser) {
            mBackStack.remove(Integer.valueOf(mSelectedItemId));
            mBackStack.add(mSelectedItemId);
        }

        if (mSnackBar != null) {
            mSnackBar.dismiss();
        }

        if (mSelectedItemId != -1) {
            findViewById(mSelectedItemId).setSelected(false);
        }
        view.setSelected(true);


        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = mSelectedItemId != -1 ? fragmentManager.findFragmentByTag(getTagById(mSelectedItemId)) : null;
        String newFragmentTag = getTagById(view.getId());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }
        Fragment newFragment = fragmentManager.findFragmentByTag(newFragmentTag);

        if (newFragment != null) {
            transaction.attach(newFragment);
        } else {
            transaction.add(R.id.container, createFragmentByTag(newFragmentTag), newFragmentTag);
        }
        transaction.commit();
        if (immediately) {
            fragmentManager.executePendingTransactions();
        }
        mSelectedItemId = view.getId();
        return true;
    }


    @NonNull
    private String getTagById(int id) {
        switch (id) {
            case R.id.home:
                return HOME_TAG;
            case R.id.team:
                return TEAM_TAG;
            case R.id.proxies:
                return PROXIES_TAG;
            case R.id.me:
                return PROFILE_TAG;
            default:
                throw new RuntimeException("unknown item id");
        }
    }


    @NonNull
    private Fragment createFragmentByTag(String tag) {
        switch (tag) {
            case HOME_TAG:
                return ADataFragment.getInstance(HOME_DATA_TAG, HomeFragment.class);
            case TEAM_TAG:
                return ADataFragment.getInstance(HOME_DATA_TAG, TeamFragment.class);
            case PROFILE_TAG:
                return ADataFragment.getInstance(HOME_DATA_TAG, UserFragment.class);
            case PROXIES_TAG:
                return ADataFragment.getInstance(HOME_DATA_TAG, ProxiesFragment.class);
            default:
                throw new RuntimeException("unknown tag " + tag);
        }
    }

    @Override
    protected String[] getDataTags() {
        return mTeam != null ? new String[]{HOME_DATA_TAG, SET_PROXY_POSITION_DATA, USER_DATA, WALLET_DATA, VOTE_DATA} : new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return mTeam != null ? new String[]{TEAMMATES_DATA_TAG, CLAIMS_DATA_TAG, FEED_DATA_TAG, MY_PROXIES_DATA, PROXIES_FOR_DATA, USER_RATING_DATA, TEAMS_DATA}
                : new String[]{};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case HOME_DATA_TAG:
                return TeambrellaDataFragment.getInstance(TeambrellaUris.getHomeUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)));
            case SET_PROXY_POSITION_DATA:
                return TeambrellaDataFragment.getInstance(null);
            case USER_DATA:
                return TeambrellaDataFragment.getInstance(TeambrellaUris.getTeammateUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID), mUserId));
            case WALLET_DATA:
                return TeambrellaDataFragment.getInstance(TeambrellaUris.getWallet(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)));
            case VOTE_DATA:
                return TeambrellaDataFragment.getInstance(null);

        }
        return null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mDisposable = getObservable(HOME_DATA_TAG).subscribe(notification -> {
            if (notification.isOnNext()) {
                JsonWrapper response = new JsonWrapper(notification.getValue());
                JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
                GlideApp.with(this).load(getImageLoader().getImageUrl(data.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                        .apply(new RequestOptions().transforms(new CenterCrop(), new CircleCrop())
                                .placeholder(R.drawable.picture_background_circle)).into(mAvatar);
                mUserName = data.getString(TeambrellaModel.ATTR_DATA_NAME);
                mFBName = data.getString(TeambrellaModel.ATTR_DATA_FB_NAME);
                mUserPicture = TeambrellaImageLoader.getImageUri(data.getString(TeambrellaModel.ATTR_DATA_AVATAR));
            }
        });
    }

    @Override
    public int getTeamId() {
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID);
    }


    @Override
    public void setProxyPosition(String userId, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(SET_PROXY_POSITION_DATA);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getSetProxyPositionUri(position, userId, mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)));
        }
    }

    @Override
    public void optInToRating(boolean optIn) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataPagerFragment dataFragment = (TeambrellaDataPagerFragment) fragmentManager.findFragmentByTag(USER_RATING_DATA);
        if (dataFragment != null) {
            dataFragment.getPager().reload(TeambrellaUris.getUserRatingUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID), optIn));
        }
    }

    @Override
    public void postVote(double vote) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(VOTE_DATA);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getTeammateVoteUri(getTeammateId(), vote));
        }

        StatisticHelper.onApplicationVote(this, getTeamId(), getTeammateId(), vote);
    }

    @Override
    public void setAsProxy(boolean set) {
        // nothing to do
    }

    @Override
    public boolean isItMe() {
        return true;
    }


    @Override
    public int getTeamType() {
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE);
    }

    @Override
    public String getTeamName() {
        return mTeam.getString(TeambrellaModel.ATTR_DATA_TEAM_NAME);
    }

    @Override
    public String getTeamLogoUri() {
        return mTeam.getString(TeambrellaModel.ATTR_DATA_TEAM_LOGO);
    }

    @Override
    public int getTeammateId() {
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_MY_TEAMMATE_ID);
    }

    @Override
    public String getInviteFriendsText() {
        return mTeam.getString(TeambrellaModel.ATTR_DATA_INVITE_FRIENDS_TEXT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.disconnect();
            mClient = null;
        }
    }

    @Override
    public void startNewDiscussion() {
        StartNewChatActivity.startForResult(this, mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID), DEFAULT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPager(FEED_DATA_TAG).reload();
        load(HOME_DATA_TAG);
        getPager(CLAIMS_DATA_TAG).reload();
        load(USER_DATA);
        getPager(MY_PROXIES_DATA).reload();
        mWalletBackupManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int getTeamAccessLevel() {
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ACCESS_LEVEL);
    }

    @Override
    public boolean isFullTeamAccess() {
        return getTeamAccessLevel() == TeambrellaModel.TeamAccessLevel.FULL_ACCESS;
    }

    @Override
    public String getUserId() {
        return mUserId;
    }

    @Override
    public String getCurrency() {
        return mTeam.getString(TeambrellaModel.ATTR_DATA_CURRENCY);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mClient != null) {
            mClient.onResume();
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
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getTeamUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        TeambrellaModel.ATTR_DATA_TEAMMATES, TeammatesDataPagerFragment.class);
            case CLAIMS_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getClaimsUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        null, ClaimsDataPagerFragment.class);
            case FEED_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getFeedUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        null, TeambrellaDataPagerFragment.class);
            case MY_PROXIES_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getMyProxiesUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        null, TeambrellaDataPagerFragment.class);
            case PROXIES_FOR_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getProxyForUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        "Members", TeambrellaDataPagerFragment.class);
            case USER_RATING_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getUserRatingUri(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)),
                        "Members", TeambrellaDataPagerFragment.class);
            case TEAMS_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getMyTeams(),
                        "MyTeams", TeambrellaDataPagerFragment.class);
        }
        return null;
    }


    @Override
    public void showSnackBar(@StringRes int text) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(findViewById(R.id.coordinator), text, Snackbar.LENGTH_LONG);

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
    public void onBackPressed() {
        Integer nextId = mBackStack.isEmpty() ? null : mBackStack.pop();
        if (nextId != null) {
            onNavigationItemSelected(findViewById(nextId), false, false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showTeamChooser() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(TEAM_CHOOSER_FRAGMENT_TAG) == null) {
            new TeamSelectionFragment().show(fragmentManager, TEAM_CHOOSER_FRAGMENT_TAG);
        }
    }


    @Override
    public String getFundAddress() {

        EtherAccount eth = getEtherAccountOrNull();
        if (eth != null) {
            return eth.getDepositAddress();
        } else {
            Log.w(LOG_TAG, "Was unnable to get direct eth address. See errors logged before this.");
            return null;
        }
    }

    @Override
    public void showWalletBackupDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(WALLET_BACKUP_FRAGMENT_TAG) == null) {
            new WalletBackupInfoFragment().show(fragmentManager, WALLET_BACKUP_FRAGMENT_TAG);
        }
    }

    private EtherAccount getEtherAccountOrNull() {
        if (mEtherAccount != null) return mEtherAccount;

        try {
            return mEtherAccount = new EtherAccount(TeambrellaUser.get(this).getPrivateKey(), this);
        } catch (CryptoException e) {
            Log.e(LOG_TAG, "Cannot get initialize local ethereum account: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void backUpWallet(boolean force) {
        mWalletBackupManager.saveWallet(mFBName,
                mUserName,
                mUserPicture,
                TeambrellaUser.get(this).getPrivateKey()
                , force);
    }

    @Override
    public void addWalletBackupListener(WalletBackupManager.IWalletBackupListener listener) {
        mWalletBackupManager.addBackupListener(listener);
    }

    @Override
    public void removeWalletBackupListener(WalletBackupManager.IWalletBackupListener listener) {
        mWalletBackupManager.removeBackupListener(listener);
    }

    @Override
    public void launchActivity(Intent intent) {
        startActivityForResult(intent, DEFAULT_REQUEST_CODE);
    }


    @Override
    public void showCoverage() {
        onNavigationItemSelected(findViewById(R.id.me), true, true);
        UserFragment userFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_TAG);
        if (userFragment != null) {
            userFragment.showCoverage();
        }
    }

    @Override
    public void showWallet() {
        onNavigationItemSelected(findViewById(R.id.me), true, true);
        UserFragment userFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_TAG);
        if (userFragment != null) {
            userFragment.showWallet();
        }
    }


    private void showFeed() {
        onNavigationItemSelected(findViewById(R.id.team), true, true);
    }


    private class MainNotificationClient extends TeambrellaNotificationServiceClient {


        private boolean mResumed;
        private boolean mPrivateMessageOnResume;
        private boolean mFeedDataOnResume;


        MainNotificationClient(Context context) {
            super(context);
        }

        @Override
        public boolean onPrivateMessage(String userId, String name, String avatar, String text) {
            if (mResumed) {
                load(HOME_DATA_TAG);
            }
            mPrivateMessageOnResume = !mResumed;
            return false;
        }

        @Override
        public boolean onPostCreated(int teamId, String userId, String topicId, String postId, String name, String avatar, String text) {

            if (mResumed) {
                getPager(FEED_DATA_TAG).reload();
                load(HOME_DATA_TAG);
            }

            mFeedDataOnResume = !mResumed;
            return false;
        }


        private void onResume() {

            mResumed = true;

            if (mPrivateMessageOnResume || mFeedDataOnResume) {
                load(HOME_DATA_TAG);
                mPrivateMessageOnResume = false;
            }

            if (mFeedDataOnResume) {
                getPager(FEED_DATA_TAG).reload();
                mFeedDataOnResume = false;
            }
        }


        private void onPause() {
            mResumed = false;
        }


    }
}


