package com.teambrella.android.ui.team.teammates;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

/**
 * Teammates Data Pager Loader
 */
public class TeammatesDataPagerFragment extends TeambrellaDataPagerFragment {
    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        TeammatesDataPagerLoader loader = new TeammatesDataPagerLoader(args.getParcelable(EXTRA_URI));
        ((TeambrellaDataHostActivity) getContext()).getComponent().inject(loader);
        return loader;
    }
}
