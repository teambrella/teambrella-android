package com.teambrella.android.ui.votes;

import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * All Votes Fragment
 */
public class AllVotesFragment extends ADataPagerProgressFragment<IAllVoteActivity> {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new AllVotesAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId());
    }
}
