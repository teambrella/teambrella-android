package com.teambrella.android.ui.team.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Feed Fragment
 */
public class FeedFragment extends ADataPagerProgressFragment<IMainDataHost> {


    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static FeedFragment getInstance(String tag, int teamId) {
        FeedFragment fragment = ADataPagerProgressFragment.getInstance(tag, FeedFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisiblePosition = ((LinearLayoutManager) (recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition();
                    if (firstVisiblePosition == 0) {
                        ((AppBarLayout) view.findViewById(R.id.appbar)).setExpanded(true, true);
                        setRefreshable(true);
                    } else {
                        setRefreshable(false);
                    }
                }
            }
        });

        view.findViewById(R.id.start_new_discussion).setOnClickListener(v -> mDataHost.startNewDiscussion());
    }

    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new FeedAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }


    @Override
    protected int getContentLayout() {
        return R.layout.fragment_feed;
    }
}
