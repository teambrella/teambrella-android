package com.teambrella.android.ui.claim;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;

/**
 * Claims Data Pager Fragment
 */
public class ClaimsDataPagerFragment extends TeambrellaDataPagerFragment {
    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ClaimsDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }
}
