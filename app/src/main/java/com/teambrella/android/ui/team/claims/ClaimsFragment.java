package com.teambrella.android.ui.team.claims;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Claims fragment
 */
public class ClaimsFragment extends ADataPagerProgressFragment<IDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static TeamClaimsFragment getInstance(String tag, int teamId) {
        TeamClaimsFragment fragment = ADataPagerProgressFragment.getInstance(tag, TeamClaimsFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
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
                    case ClaimsAdapter.VIEW_TYPE_IN_PAYMENT_HEADER:
                    case ClaimsAdapter.VIEW_TYPE_PROCESSED_HEADER:
                    case ClaimsAdapter.VIEW_TYPE_VOTED_HEADER:
                    case ClaimsAdapter.VIEW_TYPE_VOTING_HEADER:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case ClaimsAdapter.VIEW_TYPE_IN_PAYMENT_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_PROCESSED_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_VOTED_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_VOTING_HEADER:
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

    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }
}
