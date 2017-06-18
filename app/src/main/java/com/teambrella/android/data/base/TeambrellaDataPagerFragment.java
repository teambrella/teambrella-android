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

    protected static final String EXTRA_URI = "uri";
    protected static final String EXTRA_PROPERTY = "property";

    private TeambrellaDataPagerLoader mLoader;

    public static <T extends TeambrellaDataPagerFragment> T getInstance(Uri uri, String property, Class<T> clazz) {
        try {
            T fragment = clazz.newInstance();
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_URI, uri);
            args.putString(EXTRA_PROPERTY, property);
            fragment.setArguments(args);
            return fragment;
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mLoader = createLoader(getArguments());

    }

    public IDataPager<JsonArray> getPager() {
        return mLoader;
    }

    protected TeambrellaDataPagerLoader createLoader(Bundle args) {
        final Uri uri = args != null ? args.getParcelable(EXTRA_URI) : null;
        final String property = args != null ? args.getString(EXTRA_PROPERTY) : null;
        return new TeambrellaDataPagerLoader(getContext(), uri, property);
    }
}
