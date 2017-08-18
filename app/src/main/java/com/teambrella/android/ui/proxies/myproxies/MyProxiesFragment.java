package com.teambrella.android.ui.proxies.myproxies;

import android.support.v7.widget.RecyclerView;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * My Proxies Fragment
 */
public class MyProxiesFragment extends ADataPagerProgressFragment<IMainDataHost> {

    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new MyProxiesAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mDataHost.getCurrency());
    }

    @Override
    protected boolean isLongPressDragEnabled() {
        return true;
    }


    @Override
    protected void onDraggingFinished(RecyclerView.ViewHolder viewHolder) {
        super.onDraggingFinished(viewHolder);
        JsonWrapper item = new JsonWrapper(mDataHost.getPager(mTag).getLoadedData().get(viewHolder.getAdapterPosition()).getAsJsonObject());
        mDataHost.setProxyPosition(item.getString(TeambrellaModel.ATTR_DATA_USER_ID), viewHolder.getAdapterPosition());
    }
}

