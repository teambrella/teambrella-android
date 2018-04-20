package com.teambrella.android.ui.withdraw;

import android.net.Uri;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

/**
 * Withdrawals Data Pager Fragment
 */
public class WithdrawalsDataPagerFragment extends TeambrellaDataPagerFragment {
    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        final Uri uri = args != null ? args.getParcelable(EXTRA_URI) : null;
        final String property = args != null ? args.getString(EXTRA_PROPERTY) : null;
        WithdrawalsDataPagerLoader loader = new WithdrawalsDataPagerLoader(uri, property);
        ((TeambrellaDataHostActivity) getContext()).getComponent().inject(loader);
        return loader;
    }
}
