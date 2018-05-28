package com.teambrella.android.ui.team.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Feed Fragment
 */
public class FeedFragment extends AMainDataPagerProgressFragment {


    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static FeedFragment getInstance(String tag, int teamId) {
        FeedFragment fragment = ADataPagerProgressFragment.getInstance(tag, FeedFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.teambrella.android.ui.widget.DividerItemDecoration dividerItemDecoration =
                new com.teambrella.android.ui.widget.DividerItemDecoration(getContext().getResources().getDrawable(R.drawable.divder)) {
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
                            case FeedAdapterKt.VIEW_TYPE_HEADER:
                            case TeambrellaDataPagerAdapter.VIEW_TYPE_LOADING:
                            case TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR:
                            case TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM:
                                drawDivider = false;
                        }
                        return drawDivider;
                    }
                };

        mList.addItemDecoration(dividerItemDecoration);
    }


    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new KFeedAdapter(mDataHost, getArguments().getInt(EXTRA_TEAM_ID), mDataHost.getPager(mTag), mDataHost::launchActivity);
    }
}
