package com.teambrella.android.ui.proxies.proxyfor;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Proxy For Adapter
 */
public class ProxyForAdapter extends TeambrellaDataPagerAdapter {

    public ProxyForAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }
}
