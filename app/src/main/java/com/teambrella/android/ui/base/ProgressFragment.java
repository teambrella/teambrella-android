package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    @BindView(R.id.progress)
    View mProgress;


    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mData.addView(onCreateContentView(inflater, container, savedInstanceState));
        return view;
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);


    protected void setContentShown(boolean shown) {
        mProgress.setVisibility(shown ? View.GONE : View.VISIBLE);
        mContent.setVisibility(shown ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
