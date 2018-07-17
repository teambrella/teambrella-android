package com.teambrella.android.ui.teammates;

import android.content.Context;
import android.content.Intent;
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
import com.teambrella.android.ui.base.TeambrellaPagerViewModel;
import com.teambrella.android.ui.widget.AkkuratBoldTextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * All Teammates sorted by Risk
 */
public class TeammatesByRiskActivity extends ATeambrellaActivity implements ITeammateByRiskActivity {

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

    @NonNull
    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return new String[]{TEAMMATES_DATA_TAG};
    }

    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG: {
                Bundle config = ATeambrellaDataHostActivityKt.getPagerConfig(TeambrellaUris.getTeammatesUri(mTeamId, true)
                        , TeambrellaModel.ATTR_DATA_TEAMMATES);
                config.putParcelableArrayList(TeammatesByRiskViewModel.EXTRA_RANGES, mRanges);
                return config;
            }
        }
        return super.getDataPagerConfig(tag);
    }

    @Nullable
    @Override
    protected <T extends TeambrellaPagerViewModel> Class<T> getPagerViewModelClass(@NotNull String tag) {
        switch (tag) {
            case TEAMMATES_DATA_TAG:
                //noinspection unchecked
                return (Class<T>) TeammatesByRiskViewModel.class;
        }
        return super.getPagerViewModelClass(tag);
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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(AkkuratBoldTextView.getAkkuratBoldText(this, R.string.compare_team_risk));
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(TEAMMATES_UI_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.container,
                    ADataFragmentKt.createDataFragment(new String[]{TEAMMATES_DATA_TAG}, TeammatesByRiskFragment.class), TEAMMATES_UI_TAG).commit();
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
