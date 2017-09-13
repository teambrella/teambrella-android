package com.teambrella.android.ui.proxies.proxyfor;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Proxy For Fragment
 */
public class ProxyForFragment extends AMainDataPagerProgressFragment {
    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new ProxyForAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mDataHost.getCurrency());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                boolean drawDivider = true;
                switch (parent.getAdapter().getItemViewType(position)) {
                    case ProxyForAdapter.VIEW_TYPE_COMMISSION:
                    case ProxyForAdapter.VIEW_TYPE_HEADER:
                    case ProxyForAdapter.VIEW_TYPE_BOTTOM:
                    case ProxyForAdapter.VIEW_TYPE_ERROR:
                    case ProxyForAdapter.VIEW_TYPE_LOADING:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case ProxyForAdapter.VIEW_TYPE_COMMISSION:
                        case ProxyForAdapter.VIEW_TYPE_HEADER:
                        case ProxyForAdapter.VIEW_TYPE_BOTTOM:
                        case ProxyForAdapter.VIEW_TYPE_ERROR:
                        case ProxyForAdapter.VIEW_TYPE_LOADING:
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
        mList.addItemDecoration(dividerItemDecoration);
    }
}
