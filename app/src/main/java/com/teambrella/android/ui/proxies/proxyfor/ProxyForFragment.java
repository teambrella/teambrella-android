package com.teambrella.android.ui.proxies.proxyfor;

import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Proxy For Fragment
 */
public class ProxyForFragment extends ADataPagerProgressFragment<IMainDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ProxyForAdapter(mDataHost.getPager(mTag));
    }
}
