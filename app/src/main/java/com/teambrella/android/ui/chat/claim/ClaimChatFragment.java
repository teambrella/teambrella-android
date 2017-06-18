package com.teambrella.android.ui.chat.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;


/**
 * Claim Chat Fragment
 */
public class ClaimChatFragment extends ADataPagerProgressFragment<IDataHost> {


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimChatAdapter(mDataHost.getPager(mTag));
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((LinearLayoutManager) mList.getLayoutManager()).setStackFromEnd(true);
        super.onViewCreated(view, savedInstanceState);
    }
}
