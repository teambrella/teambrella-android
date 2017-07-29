package com.teambrella.android.ui.claim;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Claim Activity
 */
public class ClaimActivity extends ADataHostActivity implements IClaimActivity {

    private static final String CLAIM_DATA_TAG = "claim_data_tag";
    private static final String VOTE_DATA_TAG = "vote_data_tag";
    private static final String UI_TAG = "ui";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_MODEL = "model";
    private static final String EXTRA_TEAM_ID = "team_id";


    private Disposable mDisposal;


    int mClaimId;


    public static Intent getLaunchIntent(Context context, Uri uri, String model, int teamId) {
        return new Intent(context, ClaimActivity.class)
                .putExtra(EXTRA_URI, uri)
                .putExtra(EXTRA_MODEL, model)
                .putExtra(EXTRA_TEAM_ID, teamId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(UI_TAG) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ADataProgressFragment.getInstance(new String[]{CLAIM_DATA_TAG, VOTE_DATA_TAG}, ClaimFragment.class), UI_TAG)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.title)).setText(getIntent().getStringExtra(EXTRA_MODEL));
    }


    @Override
    protected void onStart() {
        super.onStart();
        mDisposal = getObservable(CLAIM_DATA_TAG).subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{CLAIM_DATA_TAG, VOTE_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case CLAIM_DATA_TAG:
                return TeambrellaDataFragment.getInstance(getIntent().getParcelableExtra(EXTRA_URI));
            case VOTE_DATA_TAG:
                return TeambrellaDataFragment.getInstance(null);
        }

        return null;
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
    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    @Override
    public void setSubtitle(String subtitle) {
        ((TextView) findViewById(R.id.subtitle)).setText(subtitle);
    }


    @Override
    public void postVote(int vote) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(VOTE_DATA_TAG);
        if (dataFragment != null) {
            dataFragment.load(TeambrellaUris.getClaimVoteUri(mClaimId, vote));
        }
    }

    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            if (basic != null) {
                String pictureUri = TeambrellaModel.getImage(TeambrellaServer.BASE_URL, basic.getObject(), TeambrellaModel.ATTR_DATA_AVATAR);
                if (pictureUri != null) {
                    ImageView teammatePicture = (ImageView) findViewById(R.id.teammate_picture);
                    TeambrellaImageLoader.getInstance(this).getPicasso()
                            .load(pictureUri).into(teammatePicture);
                    teammatePicture.setOnClickListener(v ->
                            TeammateActivity.start(ClaimActivity.this
                                    , getIntent().getIntExtra(EXTRA_TEAM_ID, 0)
                                    , basic.getString(TeambrellaModel.ATTR_DATA_USER_ID)
                                    , basic.getString(TeambrellaModel.ATTR_DATA_NAME)
                                    , pictureUri));
                }
            }


            mClaimId = data.getInt(TeambrellaModel.ATTR_DATA_ID, 0);

        }
    }
}
