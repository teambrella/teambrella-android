package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;


/**
 * Home Fragment
 */
public class HomeFragment extends ADataFragment<IDataHost> {

    private static final String CARDS_FRAGMENT_TAG = "cards";
    private static final String COVERAGE_FRAGMENT_TAG = "coverage";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mDataHost.load(mTags[0]);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(CARDS_FRAGMENT_TAG) == null) {
            transaction.add(R.id.top_container, ADataFragment.getInstance(mTags, HomeCardsFragment.class), CARDS_FRAGMENT_TAG);
        }

        if (fragmentManager.findFragmentByTag(COVERAGE_FRAGMENT_TAG) == null) {
            transaction.add(R.id.bottom_container, ADataFragment.getInstance(mTags, HomeCoverageAndWalletFragment.class), COVERAGE_FRAGMENT_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }

    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {

    }
}
