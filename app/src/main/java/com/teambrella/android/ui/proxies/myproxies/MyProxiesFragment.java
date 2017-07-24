package com.teambrella.android.ui.proxies.myproxies;

import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * My Proxies Fragment
 */
public class MyProxiesFragment extends ADataPagerProgressFragment<IMainDataHost> {

    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new TeambrellaDataPagerAdapter(mDataHost.getPager(mTag));
    }
}
