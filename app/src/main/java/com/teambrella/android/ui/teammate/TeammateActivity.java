package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Teammate screen.
 */
public class TeammateActivity extends ADataHostActivity implements ITeammateActivity {

    private static final String TEAMMATE_URI = "teammate_uri";
    private static final String TEAMMATE_NAME = "teammate_name";
    private static final String TEAMMATE_PICTURE = "teammate_picture";

    private static final String DATA_FRAGMENT = "data";
    private static final String VOTE_FRAGMENT = "vote";
    private static final String UI_FRAGMENT = "ui";


    private Disposable mDisposal;
    private int mTeammateId = -1;

    /**
     * Get intent to launch activity
     *
     * @param context to use
     * @param uri     teammate uri
     * @return intent to start activity
     */
    public static Intent getIntent(Context context, Uri uri, String name, String userPictureUri) {
        return new Intent(context, TeammateActivity.class)
                .putExtra(TEAMMATE_URI, uri)
                .putExtra(TEAMMATE_NAME, name)
                .putExtra(TEAMMATE_URI, uri);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_teammate);
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ADataProgressFragment.getInstance(new String[]{DATA_FRAGMENT, VOTE_FRAGMENT}, TeammateFragment.class), UI_FRAGMENT)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

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
    protected String[] getDataTags() {
        return new String[]{DATA_FRAGMENT, VOTE_FRAGMENT};
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
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT:
                return TeambrellaDataFragment
                        .getInstance(getIntent().getParcelableExtra(TEAMMATE_URI));
            case VOTE_FRAGMENT:
                return TeambrellaDataFragment.getInstance(null);
        }
        return null;
    }


    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            mTeammateId = new JsonWrapper(notification.getValue())
                    .getObject(TeambrellaModel.ATTR_DATA)
                    .getInt(TeambrellaModel.ATTR_DATA_ID, 0);
        }
    }
}
