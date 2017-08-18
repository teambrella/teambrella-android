package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Teammate screen.
 */
public class TeammateActivity extends ADataHostActivity implements ITeammateActivity {

    private static final String TEAMMATE_URI = "teammate_uri";
    private static final String TEAMMATE_NAME = "teammate_name";
    private static final String TEAMMATE_PICTURE = "teammate_picture";
    private static final String CURRENCY = "currency";

    private static final String DATA_FRAGMENT = "data";
    private static final String VOTE_FRAGMENT = "vote";
    private static final String PROXY_FRAGMENT = "proxy";
    private static final String UI_FRAGMENT = "ui";


    private Disposable mDisposal;
    private int mTeammateId = -1;
    private String mUserId = null;
    private String mCurrency;


    public static Intent getIntent(Context context, int teamId, String userId, String name, String userPictureUri, String currency) {
        return new Intent(context, TeammateActivity.class)
                .putExtra(TEAMMATE_URI, TeambrellaUris.getTeammateUri(teamId, userId))
                .putExtra(TEAMMATE_NAME, name)
                .putExtra(TEAMMATE_PICTURE, userPictureUri)
                .putExtra(CURRENCY, currency);
    }

    public static void start(Context context, int teamId, String userId, String name, String userPictureUri, String currency) {
        context.startActivity(getIntent(context, teamId, userId, name, userPictureUri, currency));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mCurrency = getIntent().getStringExtra(CURRENCY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_teammate);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setTitle(CharSequence title) {
        if (title != null) {
            SpannableString s = new SpannableString(title);
            s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            super.setTitle(s);
        }
    }


    @Override
    public void postVote(double vote) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(VOTE_FRAGMENT);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getTeammateVoteUri(mTeammateId, vote));
        }
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


    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Observable.fromArray(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA))
                    .doOnNext(node -> mTeammateId = node.getInt(TeambrellaModel.ATTR_DATA_ID))
                    .map(node -> node.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC))
                    .doOnNext(node -> mUserId = node.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                    .onErrorReturnItem(new JsonWrapper(null))
                    .blockingFirst();
        }
    }


}
