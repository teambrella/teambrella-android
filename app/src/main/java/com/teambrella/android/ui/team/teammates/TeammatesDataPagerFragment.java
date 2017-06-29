package com.teambrella.android.ui.team.teammates;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;

/**
 * Teammates Data Pager Loader
 */
public class TeammatesDataPagerFragment extends TeambrellaDataPagerFragment {
    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new TeammatesDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }
}
