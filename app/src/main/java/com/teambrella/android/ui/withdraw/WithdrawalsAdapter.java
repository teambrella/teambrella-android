package com.teambrella.android.ui.withdraw;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Withdrawals Adapter
 */
class WithdrawalsAdapter extends TeambrellaDataPagerAdapter {

    WithdrawalsAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }
}
