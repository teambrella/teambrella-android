package com.teambrella.android.ui.teammates;

import android.net.Uri;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;

import java.util.ArrayList;

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

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        final Uri uri = args != null ? args.getParcelable(EXTRA_URI) : null;
        final ArrayList<RiskRange> ranges = args != null ? args.getParcelableArrayList(EXTRA_RANGES) : null;
        return new TeammatesByRiskDataPagerLoader(getContext(), uri, ranges);
    }

}
