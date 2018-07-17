package com.teambrella.android.ui.votes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivityKt;

import org.jetbrains.annotations.NotNull;

/**
 * All Votes Activity
 */
public class AllVotesActivity extends ATeambrellaActivity implements IAllVoteActivity {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_TEAMMATE_ID = "extra_teammate_id";
    private static final String EXTRA_CLAIM_ID = "extra_claim_id";
    private static final String EXTRA_URI = "extra_uri";

    private static final String ALL_VOTES_DATA_TAG = "data_tag";
    private static final String ALL_VOTES_UI_TAG = "ui_tag";

    private Uri mUri;
    private int mTeamId;
    private int mClaimId;
    private int mTeammateId;

    public static void startClaimAllVotes(Context context, int teamId, int claimId) {
        context.startActivity(new Intent(context, AllVotesActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_CLAIM_ID, claimId)
                .putExtra(EXTRA_URI, TeambrellaUris.getAllVotesForClaim(teamId, claimId)));
    }

    public static void startTeammateAllVotes(Context context, int teamId, int teammateId) {
        context.startActivity(new Intent(context, AllVotesActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_TEAMMATE_ID, teammateId)
                .putExtra(EXTRA_URI, TeambrellaUris.getAllVotesForTeammate(teamId, teammateId)));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final Intent intent = getIntent();

        mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, -1);
        mTeammateId = intent.getIntExtra(EXTRA_TEAMMATE_ID, -1);
        mClaimId = intent.getIntExtra(EXTRA_CLAIM_ID, -1);
        mUri = intent.getParcelableExtra(EXTRA_URI);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(ALL_VOTES_UI_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.container
                    , ADataFragmentKt.createDataFragment(new String[]{ALL_VOTES_DATA_TAG}, AllVotesFragment.class)
                    , ALL_VOTES_UI_TAG).commit();
        }

        setTitle(R.string.all_votes);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
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


    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return new String[]{ALL_VOTES_DATA_TAG};
    }


    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case ALL_VOTES_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(mUri, TeambrellaModel.ATTR_DATA_VOTERS);
        }
        return super.getDataPagerConfig(tag);
    }

    @Override
    public int getTeamId() {
        return mTeamId;
    }


    @Override
    public Uri getUri() {
        return mUri;
    }
}
