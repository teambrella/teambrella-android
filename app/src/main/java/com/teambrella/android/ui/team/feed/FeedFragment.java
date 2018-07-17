package com.teambrella.android.ui.team.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Feed Fragment
 */
public class FeedFragment extends AMainDataPagerProgressFragment {


    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static FeedFragment getInstance(String tag, int teamId) {
        FeedFragment fragment = ADataFragmentKt.createDataFragment(new String[]{tag}, FeedFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        getList().addItemDecoration(dividerItemDecoration);
    }


    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new KFeedAdapter(getDataHost(), getArguments().getInt(EXTRA_TEAM_ID), dataHost.getPager(getTags()[0]), getDataHost()::launchActivity);
    }
}
