package com.teambrella.android.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonArray;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Main Data Fragment
 */
public class MainDataFragment extends Fragment {

    private static final String EXTRA_TEAM_ID = "teamId";

    private TeambrellaDataPagerLoader mTeamListDataLoader;

    public static MainDataFragment getInstance(int teamId) {
        MainDataFragment fragment = new MainDataFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TEAM_ID, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        mTeamListDataLoader = new TeambrellaDataPagerLoader(getContext(),
                TeambrellaUris.getTeamUri(args.getInt(EXTRA_TEAM_ID)),
                TeambrellaModel.ATTR_DATA_TEAMMATES);
    }

    public IDataPager<JsonArray> getTeamListPager() {
        return mTeamListDataLoader;
    }
}
