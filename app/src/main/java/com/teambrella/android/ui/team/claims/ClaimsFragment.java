package com.teambrella.android.ui.team.claims;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Claims fragment
 */
public class ClaimsFragment extends ADataPagerProgressFragment<IDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag));
    }
}
