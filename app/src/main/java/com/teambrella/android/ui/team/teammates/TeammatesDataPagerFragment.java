package com.teambrella.android.ui.team.teammates;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

import java.util.Objects;

/**
 * Teammates Data Pager Loader
 */
public class TeammatesDataPagerFragment extends TeambrellaDataPagerFragment {
    @NonNull
    @Override
    protected IDataPager<JsonArray> createLoader(@NonNull Bundle args) {
        TeammatesDataPagerLoader loader = new TeammatesDataPagerLoader(args.getParcelable(EXTRA_URI));
        ((TeambrellaDataHostActivity) Objects.requireNonNull(getContext())).getComponent().inject(loader);
        return loader;
    }
}
