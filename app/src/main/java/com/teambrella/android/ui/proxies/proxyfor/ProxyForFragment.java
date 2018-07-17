package com.teambrella.android.ui.proxies.proxyfor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.widget.DividerItemDecoration;

import io.reactivex.Notification;

/**
 * Proxy For Fragment
 */
public class ProxyForFragment extends AMainDataPagerProgressFragment {
    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new ProxyForAdapter(getDataHost().getPager(getTags()[0]), getDataHost().getTeamId(), getDataHost().getCurrency());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext().getResources().getDrawable(R.drawable.divder)) {
                    @Override
                    protected boolean canDrawChild(View view, RecyclerView parent) {
                        int position = parent.getChildAdapterPosition(view);
                        boolean drawDivider = canDrawChild(position, parent);
                        if (drawDivider && ++position < parent.getAdapter().getItemCount()) {
                            drawDivider = canDrawChild(position, parent);
                        }
                        return drawDivider;
                    }

                    private boolean canDrawChild(int position, RecyclerView parent) {
                        boolean drawDivider = true;
                        switch (parent.getAdapter().getItemViewType(position)) {
                            case ProxyForAdapter.VIEW_TYPE_COMMISSION:
                            case ProxyForAdapter.VIEW_TYPE_HEADER:
                            case ProxyForAdapter.VIEW_TYPE_BOTTOM:
                            case ProxyForAdapter.VIEW_TYPE_ERROR:
                            case ProxyForAdapter.VIEW_TYPE_EMPTY:
                            case ProxyForAdapter.VIEW_TYPE_LOADING:
                                drawDivider = false;
                        }
                        return drawDivider;
                    }
                };

        getList().addItemDecoration(dividerItemDecoration);
    }


    @Override
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            ((ProxyForAdapter) getAdapter()).setTotalCommission(data.getFloat(TeambrellaModel.ATTR_DATA_TOTAL_COMISSION));
        }
    }
}
