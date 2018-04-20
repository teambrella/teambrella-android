package com.teambrella.android.ui.proxies.myproxies;

import android.support.v7.widget.RecyclerView;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * My Proxies Fragment
 */
public class MyProxiesFragment extends AMainDataPagerProgressFragment {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new MyProxiesAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mDataHost::launchActivity, mItemTouchHelper);
    }

    @Override
    protected boolean isLongPressDragEnabled() {
        return true;
    }


    @Override
    protected void onDraggingFinished(RecyclerView.ViewHolder viewHolder) {
        super.onDraggingFinished(viewHolder);
        int position = viewHolder.getAdapterPosition();
        if (position >= 0) {
            JsonWrapper item = new JsonWrapper(mDataHost.getPager(mTag).getLoadedData().get(position).getAsJsonObject());
            mDataHost.setProxyPosition(item.getString(TeambrellaModel.ATTR_DATA_USER_ID), position);
        }
    }
}

