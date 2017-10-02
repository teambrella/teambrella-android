package com.teambrella.android.ui.teammates;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Teammates Fragment
 */
public class TeammatesByRiskFragment extends ADataPagerProgressFragment<IDataHost> {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new TeammatesByRiskAdapter(mDataHost.getPager(mTag));
    }
}
