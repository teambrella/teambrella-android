package com.teambrella.android.ui.proxies.userating;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * User Rating Fragment
 */
public class UserRatingFragment extends ADataPagerProgressFragment<IMainDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new UserRatingAdapter(mDataHost.getPager(mTag));
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.divder));
        mList.addItemDecoration(dividerItemDecoration);
    }
}
