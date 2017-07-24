package com.teambrella.android.ui.proxies.userating;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * User Rating Adapter
 */
public class UserRatingAdapter extends TeambrellaDataPagerAdapter {

    public UserRatingAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }
}
