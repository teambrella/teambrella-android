package com.teambrella.android.ui.proxies.myproxies;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * My Proxies Adapter
 */

public class MyProxiesAdapater extends TeambrellaDataPagerAdapter {

    public MyProxiesAdapater(IDataPager<JsonArray> pager) {
        super(pager);
    }
}
