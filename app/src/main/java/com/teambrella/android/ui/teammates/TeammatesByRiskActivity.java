package com.teambrella.android.ui.teammates;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

import java.util.ArrayList;

/**
 * All Teammates sorted by Risk
 */
public class TeammatesByRiskActivity extends ADataHostActivity implements ITeammateByRiskActivity {

    private static final String EXTRA_TEAM_ID = "teamId";
    private static final String EXTRA_RISK_RANGES = "risk_ranges";
    private static final String EXTRA_SELECTED = "selected";
    public static final String TEAMMATES_DATA_TAG = "teammate_data_tag";
    private static final String TEAMMATES_UI_TAG = "teammates_ui_tag";


    private int mTeamId;
    private ArrayList<RiskRange> mRanges;
    private float mSelectedValue;


    public static void start(Context context, int teamId, ArrayList<RiskRange> ranges, float selectedValue) {
        context.startActivity(new Intent(context, TeammatesByRiskActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_RISK_RANGES, ranges)
                .putExtra(EXTRA_SELECTED, selectedValue));
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{TEAMMATES_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                return TeammatesByRiskDataPagerFragment.getInstance(TeambrellaUris.getTeammatesUri(mTeamId, true), mRanges);
        }
        return null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mTeamId = getIntent().getIntExtra(EXTRA_TEAM_ID, -1);
        mRanges = getIntent().getParcelableArrayListExtra(EXTRA_RISK_RANGES);
        mSelectedValue = getIntent().getFloatExtra(EXTRA_SELECTED, 1f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString(getString(R.string.compare_team_risk));
            s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(s);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(TEAMMATES_UI_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.container,
                    ADataPagerProgressFragment.getInstance(TEAMMATES_DATA_TAG, TeammatesByRiskFragment.class), TEAMMATES_UI_TAG).commit();
        }
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


    @Override
    public int getTeamId() {
        return mTeamId;
    }


    @Override
    public float getSelectedValue() {
        return mSelectedValue;
    }
}
