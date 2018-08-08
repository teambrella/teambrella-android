package com.teambrella.android.ui.proxies.myproxies;

import androidx.recyclerview.widget.RecyclerView;

import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * My Proxies Fragment
 */
public class MyProxiesFragment extends AMainDataPagerProgressFragment {

    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new MyProxiesAdapter(getDataHost().getPager(getTags()[0]), getDataHost().getTeamId(), getDataHost()::launchActivity, getItemTouchHelper());
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
            JsonWrapper item = new JsonWrapper(getDataHost().getPager(getTags()[0]).getLoadedData().get(position).getAsJsonObject());
            getDataHost().setProxyPosition(item.getString(TeambrellaModel.ATTR_DATA_USER_ID), position);
        }
    }
}

