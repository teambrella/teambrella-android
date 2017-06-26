package com.teambrella.android.ui.team;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Members fragment
 */
public class MembersFragment extends ADataPagerProgressFragment<IDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static MembersFragment getInstance(String tag, int teamId) {
        MembersFragment fragment = ADataPagerProgressFragment.getInstance(tag, MembersFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new TeammatesRecyclerAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }
}
