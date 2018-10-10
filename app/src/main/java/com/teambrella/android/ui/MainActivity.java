package com.teambrella.android.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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
import com.teambrella.android.backup.WalletBackUpService;
import com.teambrella.android.backup.WalletBackupManager;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivityKt;
import com.teambrella.android.ui.base.TeambrellaActivityBroadcastReceiver;
import com.teambrella.android.ui.base.TeambrellaDataViewModel;
import com.teambrella.android.ui.base.TeambrellaPagerViewModel;
import com.teambrella.android.ui.chat.StartNewChatActivity;
import com.teambrella.android.ui.claim.ClaimsViewModel;
import com.teambrella.android.ui.home.HomeViewModel;
import com.teambrella.android.ui.home.KHomeFragment;
import com.teambrella.android.ui.proxies.ProxiesFragment;
import com.teambrella.android.ui.team.TeamFragment;
import com.teambrella.android.ui.team.feed.FeedViewModel;
import com.teambrella.android.ui.team.teammates.TeammatesViewModel;
import com.teambrella.android.ui.teammate.ITeammateActivity;
import com.teambrella.android.ui.teammate.TeammateViewModel;
import com.teambrella.android.ui.user.UserFragment;
import com.teambrella.android.ui.user.wallet.WalletBackupInfoFragment;
import com.teambrella.android.ui.widget.BottomBarItemView;
import com.teambrella.android.util.TeambrellaUtilService;
import com.teambrella.android.util.log.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Stack;

import io.reactivex.Notification;

import static com.teambrella.android.services.push.KPushNotifications.CREATED_POST;
import static com.teambrella.android.services.push.KPushNotifications.PRIVATE_MSG;


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
    public static final String ACTION_BACKUP_WALLET = "action_backup_wallet";


    public static final String ACTION_SHOW_I_AM_PROXY_FOR = "action_show_i_am_proxy_for";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_REQUEST_CODE = 102;


    private static final String USER_ID_EXTRA = "user_id_extra";
    private static final String TEAM_EXTRA = "team_extra";
    private static final String TEAM_ID_EXTRA = "team_id_extra";
    private static final String EXTRA_SELECTED_ITEM = "selected_item";
    private static final String EXTRA_BACKSTACK = "extra_back_stack";

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
    public static final String TEAM_NOTIFICATIONS_DATA = "team_notifications_data";


    private static final String HOME_TAG = "home";
    private static final String TEAM_TAG = "team";
    private static final String PROXIES_TAG = "proxies";
    private static final String PROFILE_TAG = "profile";
    private static final String TEAM_CHOOSER_FRAGMENT_TAG = "team_chooser";
    private static final String WALLET_BACKUP_FRAGMENT_TAG = "wallet_backup_dialog";
    private static final String NOTIFICATION_SETTINGS_FRAGMENT_TAG = "notification_settings";


    private int mSelectedItemId = -1;
    private String mUserId;
    private String mUserName;
    private String mSocialName;
    private Uri mUserPicture;
    private ImageView mAvatar;
    private JsonWrapper mTeam;
    private Snackbar mSnackBar;
    private String mUserCity;
    private String mUserTopicId;
    private MainNotificationClient mClient;
    private EtherAccount mEtherAccount;
    private int mTeamNotificationSettings = TeambrellaModel.TeamNotifications.DAILY;

    private Stack<Integer> mBackStack = new Stack<>();
    private WalletBackupManager mWalletBackupManager;

    public static Intent getLaunchIntent(Context context, String userId, String team) {
        return new Intent(context, MainActivity.class)
                .putExtra(TEAM_EXTRA, team)
                .putExtra(USER_ID_EXTRA, userId);
    }


    public static Intent getLaunchIntent(Context context, String action, int teamId) {
        return new Intent(context, MainActivity.class)
                .setAction(action).putExtra(TEAM_ID_EXTRA, teamId);
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
            mAvatar = ((BottomBarItemView) findViewById(R.id.me)).icon;
            findViewById(R.id.home).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.team).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.proxies).setOnClickListener(this::onNavigationItemSelected);
            findViewById(R.id.me).setOnClickListener(this::onNavigationItemSelected);
            onNavigationItemSelected(findViewById(savedInstanceState != null ? savedInstanceState.getInt(EXTRA_SELECTED_ITEM, R.id.home) : R.id.home), true, true);
            if (savedInstanceState == null) {
                startService(new Intent(this, TeambrellaNotificationService.class).setAction(TeambrellaNotificationService.CONNECT_ACTION));
            }
            mClient = new MainNotificationClient(this);
            mClient.connect();
            onNewIntent(intent);
        } else {
            finish();
            startActivity(WelcomeActivity.getLaunchIntent(this, intent.getAction()
                    , intent.getIntExtra(TEAM_ID_EXTRA, -1)));
        }

        TeambrellaUtilService.scheduleWalletSync(this);
        TeambrellaUtilService.scheduleCheckingSocket(this);
        TeambrellaUtilService.oneoffWalletSync(this);

        if (savedInstanceState == null) {
            WalletBackUpService.Companion.scheduleBackupCheck(this);
            WalletBackUpService.Companion.schedulePeriodicBackupCheck(this);
        } else {
            ArrayList<Integer> backStack = savedInstanceState.getIntegerArrayList(EXTRA_BACKSTACK);
            if (backStack != null) {
                for (Integer i : backStack) {
                    mBackStack.push(i);
                }
            }
        }

        getComponent().inject(this);
        mWalletBackupManager = new WalletBackupManager(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getObservable(HOME_DATA_TAG).observe(this, mHomeObserver);
        getObservable(USER_DATA).observe(this, mUserObserver);
        getObservable(TEAM_NOTIFICATIONS_DATA).observe(this, mTeamNotificationObserver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action != null) {
            JsonWrapper team = intent.hasExtra(TEAM_EXTRA) ? new JsonWrapper(new Gson()
                    .fromJson(intent.getStringExtra(TEAM_EXTRA)
                            , JsonObject.class)) : null;

            int teamId = team != null ? team.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID, intent.getIntExtra(TEAM_ID_EXTRA, -1)) :
                    intent.getIntExtra(TEAM_ID_EXTRA, -1);

            if (teamId == -1 || teamId == getTeamId()) {
                switch (action) {
                    case ACTION_SHOW_WALLET:
                        showWallet();
                        break;
                    case ACTION_BACKUP_WALLET:
                        showWallet();
                        showWalletBackupDialog();
                        break;
                    case ACTION_SHOW_FEED:
                        showFeed();
                        break;

                    case ACTION_SHOW_I_AM_PROXY_FOR:
                        showIAmProxyFor();
                        break;

                }
                load(HOME_DATA_TAG);
            } else {
                finish();
                if (team != null) {
                    startActivity(intent);
                } else {
                    startActivity(WelcomeActivity.getLaunchIntent(this, intent.getAction(), teamId));
                }
            }
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
                return ADataFragmentKt.createDataFragment(new String[]{HOME_DATA_TAG}, KHomeFragment.class);
            case TEAM_TAG:
                return ADataFragmentKt.createDataFragment(new String[]{HOME_DATA_TAG, TEAM_NOTIFICATIONS_DATA}, TeamFragment.class);
            case PROFILE_TAG:
                return ADataFragmentKt.createDataFragment(new String[]{HOME_DATA_TAG}, UserFragment.class);
            case PROXIES_TAG:
                return ADataFragmentKt.createDataFragment(new String[]{HOME_DATA_TAG}, ProxiesFragment.class);
            default:
                throw new RuntimeException("unknown tag " + tag);
        }
    }

    @NonNull
    @Override
    protected String[] getDataTags() {
        return mTeam != null ? new String[]{HOME_DATA_TAG, SET_PROXY_POSITION_DATA, USER_DATA, WALLET_DATA, VOTE_DATA, TEAM_NOTIFICATIONS_DATA} : new String[]{};
    }

    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return mTeam != null ? new String[]{TEAMMATES_DATA_TAG, CLAIMS_DATA_TAG, FEED_DATA_TAG, MY_PROXIES_DATA, PROXIES_FOR_DATA, USER_RATING_DATA, TEAMS_DATA}
                : new String[]{};
    }

    @Nullable
    @Override
    protected <T extends TeambrellaPagerViewModel> Class<T> getPagerViewModelClass(@NotNull String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) TeammatesViewModel.class;
            case CLAIMS_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) ClaimsViewModel.class;
            case FEED_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) FeedViewModel.class;
            default:
                return super.getPagerViewModelClass(tag);
        }
    }

    @Nullable
    @Override
    protected <T extends TeambrellaDataViewModel> Class<T> getDataViewModelClass(@NotNull String tag) {
        switch (tag) {
            case HOME_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) HomeViewModel.class;
            case USER_DATA:
                //noinspection unchecked
                return (Class<T>) TeammateViewModel.class;
        }
        return super.getDataViewModelClass(tag);
    }

    @Nullable
    @Override
    protected Bundle getDataConfig(@NotNull String tag) {
        switch (tag) {
            case HOME_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getHomeUri(getTeamId()), true);
            case SET_PROXY_POSITION_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig();
            case USER_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getTeammateUri(getTeamId(), mUserId), true);
            case WALLET_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getWallet(getTeamId()));
            case VOTE_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig();
            case TEAM_NOTIFICATIONS_DATA:
                return ATeambrellaDataHostActivityKt.getDataConfig(TeambrellaUris.getNotificationSettingUri(getTeamId()), true);
            default:
                return super.getDataConfig(tag);
        }
    }

    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getTeamUri(getTeamId()), TeambrellaModel.ATTR_DATA_TEAMMATES);
            case CLAIMS_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getClaimsUri(getTeamId()));
            case FEED_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getFeedUri(getTeamId()));
            case MY_PROXIES_DATA:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getMyProxiesUri(getTeamId()));
            case PROXIES_FOR_DATA:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getProxyForUri(getTeamId()), TeambrellaModel.ATTR_DATA_MEMBERS);
            case USER_RATING_DATA:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getUserRatingUri(getTeamId()), TeambrellaModel.ATTR_DATA_MEMBERS);
            case TEAMS_DATA:
                return ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getMyTeams(), "MyTeams");
            default:
                return super.getDataPagerConfig(tag);
        }
    }

    @Override
    public int getTeamId() {
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID);
    }


    @Override
    public void setProxyPosition(String userId, int position) {
        TeambrellaDataViewModel model = ViewModelProviders.of(this).get(SET_PROXY_POSITION_DATA, TeambrellaDataViewModel.class);
        model.load(TeambrellaUris.getSetProxyPositionUri(position, userId, getTeamId()));
    }

    @Override
    public void optInToRating(boolean optIn) {
        TeambrellaPagerViewModel model = ViewModelProviders.of(this).get(USER_RATING_DATA, TeambrellaPagerViewModel.class);
        model.dataPager.reload(TeambrellaUris.getUserRatingUri(getTeamId(), optIn));

    }

    @Override
    public void postVote(double vote) {
        TeambrellaDataViewModel model = ViewModelProviders.of(this).get(VOTE_DATA, TeambrellaDataViewModel.class);
        model.load(TeambrellaUris.getTeammateVoteUri(getTeammateId(), vote));
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
    public String getUserCity() {
        return mUserCity;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_ITEM, mSelectedItemId);
        outState.putIntegerArrayList(EXTRA_BACKSTACK, new ArrayList<>(mBackStack));
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
        if (mUserName != null && mUserPicture != null && mSocialName != null) {
            mWalletBackupManager.saveWallet(mSocialName,
                    mUserName,
                    mUserPicture,
                    TeambrellaUser.get(this).getPrivateKey()
                    , force);
        }
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

    private void showIAmProxyFor() {
        onNavigationItemSelected(findViewById(R.id.proxies), true, true);
        ProxiesFragment userFragment = (ProxiesFragment) getSupportFragmentManager().findFragmentByTag(PROXIES_TAG);
        if (userFragment != null) {
            userFragment.showIAmProxyFor();
        }
    }


    private void markTopicRead(String topicId) {
        ViewModelProvider viewModelProvider = ViewModelProviders.of(this);
        viewModelProvider.get(FEED_DATA_TAG, FeedViewModel.class).markTopicRead(topicId);
        viewModelProvider.get(HOME_DATA_TAG, HomeViewModel.class).markTopicRead(topicId);
        viewModelProvider.get(USER_DATA, TeammateViewModel.class).markTopicRead(topicId);
    }


    private class MainNotificationClient extends TeambrellaNotificationServiceClient {


        private boolean mResumed;
        private boolean mPrivateMessageOnResume;
        private boolean mFeedDataOnResume;
        private boolean mUserDataOnResume;


        MainNotificationClient(Context context) {
            super(context);
        }

        @Override
        public boolean onPushMessage(INotificationMessage message) {

            switch (message.getCmd()) {
                case PRIVATE_MSG:
                    if (mResumed) {
                        load(HOME_DATA_TAG);
                    }
                    mPrivateMessageOnResume = !mResumed;
                    break;

                case CREATED_POST:
                    if (mResumed) {
                        getPager(FEED_DATA_TAG).reload();
                        load(HOME_DATA_TAG);
                        if (mUserTopicId != null &&
                                mUserTopicId.equals(message.getTopicId())) {
                            load(USER_DATA);
                        }
                    } else {
                        if (mUserTopicId != null &&
                                mUserTopicId.equals(message.getTopicId())) {
                            mUserDataOnResume = true;
                        }
                    }
                    mFeedDataOnResume = !mResumed;
                    break;
            }

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

            if (mUserDataOnResume) {
                load(USER_DATA);
                mUserDataOnResume = false;
            }
        }


        private void onPause() {
            mResumed = false;
        }


    }

    private class MainTeambrellaBroadcastReceiver extends TeambrellaActivityBroadcastReceiver {
        private boolean mReloadClaims = false;

        @Override
        protected void onTopicRead(@NotNull String topicId) {
            super.onTopicRead(topicId);
            markTopicRead(topicId);
        }

        @Override
        protected void onProxyListChanged() {
            getPager(MY_PROXIES_DATA).reload();
        }

        @Override
        protected void onClaimVote(int claimId) {
            if (getStarted()) {
                getPager(CLAIMS_DATA_TAG).reload();
            } else {
                mReloadClaims = true;
            }
        }

        @Override
        protected void onPrivateMessageRead(@NotNull String userId) {
            super.onPrivateMessageRead(userId);
            load(HOME_DATA_TAG);
        }

        @Override
        protected void onClaimSubmitted() {
            super.onClaimSubmitted();
            load(HOME_DATA_TAG);
            getPager(FEED_DATA_TAG).reload();
        }

        @Override
        protected void onDiscussionStarted() {
            super.onDiscussionStarted();
            load(HOME_DATA_TAG);
            getPager(FEED_DATA_TAG).reload();
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mReloadClaims) {
                getPager(CLAIMS_DATA_TAG).reload();
                mReloadClaims = false;
            }
        }
    }

    @Override
    public void showTeamNotificationSettingsDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(NOTIFICATION_SETTINGS_FRAGMENT_TAG) == null) {
            new TeamNotificationsSettingsDialogFragment().show(fragmentManager, NOTIFICATION_SETTINGS_FRAGMENT_TAG);
        }
    }


    @Override
    public void setTeamNotificationSettings(int value) {
        ViewModelProviders.of(this).get(TEAM_NOTIFICATIONS_DATA, TeambrellaDataViewModel.class)
                .load(TeambrellaUris.getUpdateNotificationSettingUri(getTeamId(), value));
    }

    @Override
    public int getTeamNotificationSettings() {
        return mTeamNotificationSettings;
    }

    public MainActivity() {
        registerLifecycleCallback(new MainTeambrellaBroadcastReceiver());
    }


    private Observer<Notification<JsonObject>> mHomeObserver = notification -> {
        if (notification != null && notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            GlideApp.with(this).load(getImageLoader().getImageUrl(data.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .apply(new RequestOptions().transforms(new CenterCrop(), new CircleCrop())
                            .placeholder(R.drawable.picture_background_circle)).into(mAvatar);
            mUserName = data.getString(TeambrellaModel.ATTR_DATA_NAME);
            mSocialName = data.getString(TeambrellaModel.ATTR_DATA_SOCIAL_NAME);
            mUserPicture = TeambrellaImageLoader.getImageUri(data.getString(TeambrellaModel.ATTR_DATA_AVATAR));
        }
    };

    private Observer<Notification<JsonObject>> mUserObserver = notification -> {
        if (notification != null && notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            JsonWrapper discussion = data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
            String location = basic.getString(TeambrellaModel.ATTR_DATA_CITY);
            String[] locations = location != null ? location.split(",") : null;
            mUserCity = locations != null ? locations[0] : null;
            mUserTopicId = discussion.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID);
        }
    };

    private Observer<Notification<JsonObject>> mTeamNotificationObserver = notification -> {
        if (notification != null && notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mTeamNotificationSettings = data.getInt(TeambrellaModel.ATTR_DATA_NEW_TEAMMATES_NOTIFICATION
                    , TeambrellaModel.TeamNotifications.DAILY);
        }
    };


}


