package com.teambrella.android.ui.team.claims;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Claims fragment
 */
public class ClaimsFragment extends ADataPagerProgressFragment<IDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";


    public static ClaimsFragment getInstance(String tag, int teamId) {
        ClaimsFragment fragment = ADataPagerProgressFragment.getInstance(tag, ClaimsFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }
}
