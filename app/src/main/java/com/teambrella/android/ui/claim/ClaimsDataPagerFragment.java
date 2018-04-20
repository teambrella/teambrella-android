package com.teambrella.android.ui.claim;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

/**
 * Claims Data Pager Fragment
 */
public class ClaimsDataPagerFragment extends TeambrellaDataPagerFragment {
    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        ClaimsDataPagerLoader loader = new ClaimsDataPagerLoader(args.getParcelable(EXTRA_URI));
        ((TeambrellaDataHostActivity) getContext()).getComponent().inject(loader);
        return loader;
    }
}
