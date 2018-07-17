package com.teambrella.android.ui.teammates;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;

/**
 * Teammates Fragment
 */
public class TeammatesByRiskFragment extends ADataPagerProgressFragment<ITeammateByRiskActivity> {


    private boolean mFirstUpdate = true;

    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new TeammatesByRiskAdapter(getDataHost().getPager(getTags()[0]), getDataHost().getTeamId());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                boolean drawDivider = true;
                switch (parent.getAdapter().getItemViewType(position)) {
                    case TeammatesByRiskAdapter.VIEW_TYPE_HEADER:
                    case TeammatesByRiskAdapter.VIEW_TYPE_LOADING:
                    case TeammatesByRiskAdapter.VIEW_TYPE_ERROR:
                    case TeammatesByRiskAdapter.VIEW_TYPE_BOTTOM:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case TeammatesByRiskAdapter.VIEW_TYPE_HEADER:
                        case TeammatesByRiskAdapter.VIEW_TYPE_LOADING:
                        case TeammatesByRiskAdapter.VIEW_TYPE_ERROR:
                        case TeammatesByRiskAdapter.VIEW_TYPE_BOTTOM:
                            drawDivider = false;
                    }
                }

                if (position != parent.getAdapter().getItemCount() - 1
                        && drawDivider) {
                    super.getItemOffsets(outRect, view, parent, state);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        };

        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.divder));
        getList().addItemDecoration(dividerItemDecoration);
    }


    @Override
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext() && mFirstUpdate) {
            IDataPager<JsonArray> pager = getDataHost().getPager(getTags()[0]);
            for (int i = 0; i < pager.getLoadedData().size(); i++) {
                JsonWrapper item = new JsonWrapper(pager.getLoadedData().get(i).getAsJsonObject());
                switch (item.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE)) {
                    case TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_RISK:
                        if (item.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE) <= getDataHost().getSelectedValue()
                                && item.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE) >= getDataHost().getSelectedValue()) {
                            LinearLayoutManager manager = (LinearLayoutManager) getList().getLayoutManager();
                            manager.scrollToPositionWithOffset(i, 0);
                            break;
                        }
                }
            }
            mFirstUpdate = false;
        }
    }
}
