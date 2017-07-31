package com.teambrella.android.ui.team.claims;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;

/**
 * Claims Activity
 */
public class ClaimsActivity extends ADataHostActivity {

    private static final String EXTRA_URI = "uri";
    private static final String CLAIMS_DATA_TAG = "claims_data_tag";
    private static final String CLAIMS_UI_TAG = "claims_ui_tag";


    private Uri mUri;

    public static Intent getLaunchIntent(Context context, int teamId, int teammateId) {
        return new Intent(context, ClaimsActivity.class).putExtra(EXTRA_URI, TeambrellaUris.getClaimsUri(teamId, teammateId));
    }

    public static void start(Context context, int teamId, int teammateId) {
        context.startActivity(new Intent(context, ClaimsActivity.class).putExtra(EXTRA_URI, TeambrellaUris.getClaimsUri(teamId, teammateId)));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mUri = getIntent().getParcelableExtra(EXTRA_URI);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.team_claims);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, ADataPagerProgressFragment.getInstance(CLAIMS_DATA_TAG, ClaimsFragment.class), CLAIMS_UI_TAG)
                .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{CLAIMS_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case CLAIMS_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(mUri, null, TeambrellaDataPagerFragment.class);
        }
        return null;
    }
}
