package com.teambrella.android.ui.withdraw;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Withdrawals Fragment
 */
public class WithdrawalsFragment extends ADataPagerProgressFragment<IDataHost> {
    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new WithdrawalsAdapter(mDataHost.getPager(mTag));
    }
}
