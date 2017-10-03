package com.teambrella.android.ui.teammates;

import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Teammates Fragment
 */
public class TeammatesByRiskFragment extends ADataPagerProgressFragment<ITeammateByRiskActivity> {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new TeammatesByRiskAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId());
    }
}
