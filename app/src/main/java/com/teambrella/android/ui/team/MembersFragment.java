package com.teambrella.android.ui.team;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Members fragment
 */
public class MembersFragment extends ADataPagerProgressFragment<IDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new TeammatesRecyclerAdapter(mDataHost.getPager(mTag));
    }
}
