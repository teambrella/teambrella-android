package com.teambrella.android.ui.proxies.userating;

import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * User Rating Fragment
 */
public class UserRatingFragment extends ADataPagerProgressFragment<IMainDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new UserRatingAdapter(mDataHost.getPager(mTag));
    }
}
