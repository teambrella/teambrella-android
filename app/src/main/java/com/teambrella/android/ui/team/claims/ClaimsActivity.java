package com.teambrella.android.ui.team.claims;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.ATeambrellaDataHostActivityKt;
import com.teambrella.android.ui.base.TeambrellaPagerViewModel;
import com.teambrella.android.ui.claim.ClaimsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * Claims Activity
 */
public class ClaimsActivity extends ATeambrellaActivity {

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_TEAM_ID = "teamId";
    private static final String EXTRA_TEAMMATE_ID = "teammateId";
    private static final String EXTRA_CURRENCY = "currency";
    private static final String CLAIMS_DATA_TAG = "claims_data_tag";
    private static final String CLAIMS_UI_TAG = "claims_ui_tag";


    private Uri mUri;

    public static Intent getLaunchIntent(Context context, int teamId, int teammateId, String currency) {
        return new Intent(context, ClaimsActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_TEAMMATE_ID, teammateId)
                .putExtra(EXTRA_CURRENCY, currency)
                .putExtra(EXTRA_URI, TeambrellaUris.getClaimsUri(teamId, teammateId));
    }

    public static void start(Context context, int teamId, int teammateId, String currency) {
        context.startActivity(getLaunchIntent(context, teamId, teammateId, currency));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        mUri = intent.getParcelableExtra(EXTRA_URI);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.team_claims);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, ClaimsFragment.getInstance(CLAIMS_DATA_TAG, intent.getIntExtra(EXTRA_TEAM_ID, 0), intent.getStringExtra(EXTRA_CURRENCY)), CLAIMS_UI_TAG)
                .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return new String[]{CLAIMS_DATA_TAG};
    }

    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case CLAIMS_DATA_TAG:
                return ATeambrellaDataHostActivityKt.getPagerConfig(mUri);
        }
        return super.getDataPagerConfig(tag);
    }

    @Nullable
    @Override
    protected <T extends TeambrellaPagerViewModel> Class<T> getPagerViewModelClass(@NotNull String tag) {
        switch (tag) {
            case CLAIMS_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) ClaimsViewModel.class;
        }
        return super.getPagerViewModelClass(tag);
    }
}
