package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambrella.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Progress Fragment
 */
public abstract class ProgressFragment extends Fragment {

    @BindView(R.id.content)
    ViewGroup mContent;
    @BindView(R.id.data)
    ViewGroup mData;
    @BindView(R.id.error)
    ViewGroup mError;
    @BindView(R.id.refreshable)
    SwipeRefreshLayout mRefreshable;


    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mData.addView(onCreateContentView(inflater, container, savedInstanceState));
        mRefreshable.setRefreshing(true);
        return view;
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);


    protected void setContentShown(boolean shown) {
        mRefreshable.setRefreshing(!shown);
        mContent.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshable.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mRefreshable.setOnRefreshListener(this::onReload);
    }

    protected void setRefreshable(@SuppressWarnings("SameParameterValue") boolean refreshable) {
        mRefreshable.setEnabled(refreshable);
    }

    protected void setRefreshing(@SuppressWarnings("SameParameterValue") boolean refreshing) {
        mRefreshable.setRefreshing(refreshing);
    }


    protected abstract void onReload();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
