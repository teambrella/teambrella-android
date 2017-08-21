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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import com.teambrella.android.ui.claim.ClaimsDataPagerFragment;
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


    private static final String HOME_TAG = "home";
    private static final String TEAM_TAG = "team";
    private static final String PROXIES_TAG = "proxies";
    private static final String PROFILE_TAG = "profile";
    private static final String TEAM_CHOOSER_FRAGMENT_TAG = "team_chooser";


    private int mSelectedItemId = -1;
    private String mUserId;
    private Disposable mDisposable;
    private ImageView mAvatar;
    private JsonWrapper mTeam;


    public static Intent getLaunchIntent(Context context, String userId, String team) {
        return new Intent(context, MainActivity.class)
                .putExtra(TEAM_EXTRA, team)
                .putExtra(USER_ID_EXTRA, userId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID_EXTRA);

        mTeam = new JsonWrapper(new Gson()
                .fromJson(intent.getStringExtra(TEAM_EXTRA)
                        , JsonObject.class));

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
                    .putExtra(TeambrellaNotificationService.EXTRA_TEAM_ID, mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID))
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
                return TeamFragment.getInstance(mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
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
        return new String[]{TEAMMATES_DATA_TAG, CLAIMS_DATA_TAG, FEED_DATA_TAG, MY_PROXIES_DATA, PROXIES_FOR_DATA, USER_RATING_DATA, TEAMS_DATA};
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
        return mTeam.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE);
    }

    @Override
    public String getTeamName() {
        return mTeam.getString(TeambrellaModel.ATTR_DATA_TEAM_NAME);
    }

    @Override
    public String getTeamLogoUri() {
        return TeambrellaServer.BASE_URL + mTeam.getString(TeambrellaModel.ATTR_DATA_TEAM_LOGO);
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
        StartNewChatActivity.startForResult(this, mTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID), NEW_DISCUSSION_REQUEST_CODE);
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
    public void showTeamChooser() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(TEAM_CHOOSER_FRAGMENT_TAG) == null) {
            new TeamSelectionFragment().show(fragmentManager, TEAM_CHOOSER_FRAGMENT_TAG);
        }
    }
}


