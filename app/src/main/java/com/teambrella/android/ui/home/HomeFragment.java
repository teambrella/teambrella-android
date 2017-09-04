package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.util.ConnectivityUtils;

import io.reactivex.Notification;


/**
 * Home Fragment
 */
public class HomeFragment extends AMainLandingFragment {

    private static final String CARDS_FRAGMENT_TAG = "cards";
    private static final String COVERAGE_FRAGMENT_TAG = "coverage";


    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.refreshable);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(() -> mDataHost.load(mTags[0]));
        mDataHost.load(mTags[0]);
        return view;
    }

    @Override
    public void onStart() {
        mSwipeRefreshLayout.postDelayed(mRefreshingRunnable, 100);
        super.onStart();
    }

    @Override
    public void onStop() {
        mSwipeRefreshLayout.removeCallbacks(mRefreshingRunnable);
        super.onStop();
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        mSwipeRefreshLayout.removeCallbacks(mRefreshingRunnable);
        mSwipeRefreshLayout.setRefreshing(false);
        if (notification.isOnError()) {
            mDataHost.showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext()) ? R.string.something_went_wrong_error : R.string.no_internet_connection);
        }
    }


    public void setRefreshingEnable(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }


    private Runnable mRefreshingRunnable = () -> mSwipeRefreshLayout.setRefreshing(true);

}
