package com.teambrella.android.ui.base;

import android.content.Context;

import com.teambrella.android.ui.IMainDataHost;

/**
 * Main Progress Fragment
 */
public abstract class MainProgressFragment extends ProgressFragment {

    /**
     * Data Host
     */
    protected IMainDataHost mDataHost;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (IMainDataHost) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }
}
