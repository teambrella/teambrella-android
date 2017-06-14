package com.teambrella.android.data.base;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonArray;

/**
 * Teambrella Data Pager Fragment
 */
public class TeambrellaDataPagerFragment extends Fragment {

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_PROPERTY = "property";

    private TeambrellaDataPagerLoader mLoader;

    public static TeambrellaDataPagerFragment getInstance(Uri uri, String property) {
        TeambrellaDataPagerFragment fragment = new TeambrellaDataPagerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        args.putString(EXTRA_PROPERTY, property);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        final Uri uri = args != null ? args.getParcelable(EXTRA_URI) : null;
        final String property = args != null ? args.getString(EXTRA_PROPERTY) : null;
        mLoader = new TeambrellaDataPagerLoader(getContext(), uri, property);
    }

    public IDataPager<JsonArray> getPager() {
        return mLoader;
    }
}
