package com.teambrella.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.services.TeambrellaNotificationService;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.chat.StartNewChatActivity;
import com.teambrella.android.ui.home.HomeFragment;
import com.teambrella.android.ui.proxies.ProxiesFragment;
import com.teambrella.android.ui.team.TeamFragment;
import com.teambrella.android.ui.team.teammates.TeammatesDataPagerFragment;
import com.teambrella.android.ui.teammate.ITeammateActivity;
import com.teambrella.android.ui.user.UserFragment;

import io.reactivex.disposables.Disposable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Main Activity
 */
public class MainActivity extends ADataHostActivity implements IMainDataHost, ITeammateActivity {

    private static final int NEW_DISCUSSION_REQUEST_CODE = 102;


    private static final String TEAM_ID_EXTRA = "team_id";
    private static final String USER_ID_EXTRA = "user_id_extra";
    private static final String TEAM_LOG_EXTRA = "team_logo";
    private static final String TEAM_TYPE_EXTRA = "team_type";
    private static final String TEAM_NAME_EXTRA = "team_name";


    public static final String TEAMMATES_DATA_TAG = "teammates";
    public static final String CLAIMS_DATA_TAG = "claims";
    public static final String HOME_DATA_TAG = "home_data";
    public static final String FEED_DATA_TAG = "feed_data";
    public static final String MY_PROXIES_DATA = "my_proxies_data";
    public static final String PROXIES_FOR_DATA = "proxies_for_data";
    public static final String USER_RATING_DATA = "user_rating_data";
    public static final String SET_PROXY_POSITION_DATA = "set_proxy_position";
    public static final String USER_DATA = "user_data";

    private static final String HOME_TAG = "home";
    private static final String TEAM_TAG = "team";
    private static final String PROXIES_TAG = "proxies";
    private static final String PROFILE_TAG = "profile";


    private int mSelectedItemId = -1;
    private int mTeamId;
    private String mUserId;
    private int mTeamType;
    private String mTeamName;
    private Disposable mDisposable;
    private ImageView mAvatar;
    private String mTeamLogo;


    public static Intent getLaunchIntent(Context context, int teamId, String userId, String teamLogo, String teamName, int teamType) {
        return new Intent(context, MainActivity.class)
                .putExtra(TEAM_ID_EXTRA, teamId)
                .putExtra(USER_ID_EXTRA, userId)
                .putExtra(TEAM_LOG_EXTRA, teamLogo)
                .putExtra(TEAM_TYPE_EXTRA, teamType)
                .putExtra(TEAM_NAME_EXTRA, teamName);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        mTeamId = intent.getIntExtra(TEAM_ID_EXTRA, 0);
        mUserId = intent.getStringExtra(USER_ID_EXTRA);
        mTeamLogo = intent.getStringExtra(TEAM_LOG_EXTRA);
        mTeamName = intent.getStringExtra(TEAM_NAME_EXTRA);
        mTeamType = intent.getIntExtra(TEAM_TYPE_EXTRA, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAvatar = findViewById(R.id.avatar);
        findViewById(R.id.home).setOnClickListener(this::onNavigationItemSelected);
        findViewById(R.id.team).setOnClickListener(this::onNavigationItemSelected);
        findViewById(R.id.proxies).setOnClickListener(this::onNavigationItemSelected);
        findViewById(R.id.me).setOnClickListener(this::onNavigationItemSelected);
        onNavigationItemSelected(findViewById(R.id.home));

        if (savedInstanceState == null) {
            startService(new Intent(this, TeambrellaNotificationService.class)
                    .putExtra(TeambrellaNotificationService.EXTRA_TEAM_ID, mTeamId)
                    .setAction(TeambrellaNotificationService.CONNECT_ACTION));
        }
    }


    private boolean onNavigationItemSelected(View view) {

        if (mSelectedItemId == view.getId()) {
            return false;
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
        fragmentManager.executePendingTransactions();
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
                return TeamFragment.getInstance(getIntent().getIntExtra(TEAM_ID_EXTRA, 0));
            case PROFILE_TAG:
                return new UserFragment();
            case PROXIES_TAG:
                return new ProxiesFragment();
            default:
                throw new RuntimeException("unknown tag " + tag);
        }
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{HOME_DATA_TAG, SET_PROXY_POSITION_DATA, USER_DATA};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{TEAMMATES_DATA_TAG, CLAIMS_DATA_TAG, FEED_DATA_TAG, MY_PROXIES_DATA, PROXIES_FOR_DATA, USER_RATING_DATA};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case HOME_DATA_TAG:
                return TeambrellaDataFragment.getInstance(TeambrellaUris.getHomeUri(mTeamId));
            case SET_PROXY_POSITION_DATA:
                return TeambrellaDataFragment.getInstance(null);
            case USER_DATA:
                return TeambrellaDataFragment.getInstance(TeambrellaUris.getTeammateUri(mTeamId, mUserId));
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
                TeambrellaImageLoader.getInstance(MainActivity.this).getPicasso()
                        .load(TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_AVATAR))
                        .transform(new CropCircleTransformation())
                        .into(mAvatar);
            }
        });
    }

    @Override
    public int getTeamId() {
        return mTeamId;
    }


    @Override
    public void setProxyPosition(String userId, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(SET_PROXY_POSITION_DATA);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getSetProxyPositionUri(position, userId, mTeamId));
        }
    }

    @Override
    public void optInToRating(boolean optIn) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataPagerFragment dataFragment = (TeambrellaDataPagerFragment) fragmentManager.findFragmentByTag(USER_RATING_DATA);
        if (dataFragment != null) {
            dataFragment.getPager().reload(TeambrellaUris.getUserRatingUri(mTeamId, optIn));
        }
    }

    @Override
    public void postVote(double vote) {
        // nothing to do
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
        return mTeamType;
    }

    @Override
    public String getTeamName() {
        return mTeamName;
    }

    @Override
    public String getTeamLogoUri() {
        return TeambrellaServer.BASE_URL + mTeamLogo;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }


    @Override
    public void startNewDiscussion() {
        StartNewChatActivity.startForResult(this, mTeamId, NEW_DISCUSSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == NEW_DISCUSSION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            getPager(FEED_DATA_TAG).reload();
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            startService(new Intent(this, TeambrellaNotificationService.class)
                    .setAction(TeambrellaNotificationService.STOP_ACTION));
        }
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getTeamUri(mTeamId),
                        TeambrellaModel.ATTR_DATA_TEAMMATES, TeammatesDataPagerFragment.class);
            case CLAIMS_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getClaimsUri(mTeamId),
                        null, TeambrellaDataPagerFragment.class);
            case FEED_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getFeedUri(mTeamId),
                        null, TeambrellaDataPagerFragment.class);
            case MY_PROXIES_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getMyProxiesUri(mTeamId),
                        null, TeambrellaDataPagerFragment.class);
            case PROXIES_FOR_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getProxyForUri(mTeamId),
                        "Members", TeambrellaDataPagerFragment.class);
            case USER_RATING_DATA:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getUserRatingUri(mTeamId),
                        "Members", TeambrellaDataPagerFragment.class);
        }
        return null;
    }


}


