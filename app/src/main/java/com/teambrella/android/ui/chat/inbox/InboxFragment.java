package com.teambrella.android.ui.chat.inbox;

import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Inbox Fragment
 */
public class InboxFragment extends ADataPagerProgressFragment<IDataHost> {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new InboxAdapter(mDataHost.getPager(InboxActivity.INBOX_DATA_TAG));
    }
}


