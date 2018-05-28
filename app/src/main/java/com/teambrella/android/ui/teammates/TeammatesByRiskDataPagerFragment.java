package com.teambrella.android.ui.teammates;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Teammates by Risk Data Pager Fragment
 */
public class TeammatesByRiskDataPagerFragment extends TeambrellaDataPagerFragment {


    private static final String EXTRA_RANGES = "extra_ranges";


    public static TeammatesByRiskDataPagerFragment getInstance(Uri uri, ArrayList<RiskRange> ranges) {
        TeammatesByRiskDataPagerFragment fragment = new TeammatesByRiskDataPagerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        args.putParcelableArrayList(EXTRA_RANGES, ranges);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    protected IDataPager<JsonArray> createLoader(@NonNull Bundle args) {
        final Uri uri = args.getParcelable(EXTRA_URI);
        final ArrayList<RiskRange> ranges = args.getParcelableArrayList(EXTRA_RANGES);
        TeammatesByRiskDataPagerLoader loader = new TeammatesByRiskDataPagerLoader(uri, ranges);
        ((TeambrellaDataHostActivity) Objects.requireNonNull(getContext())).getComponent().inject(loader);
        return loader;
    }

}
