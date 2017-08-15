package com.teambrella.android.ui.team.teammates;

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
 * Members fragment
 */
public class MembersFragment extends ADataPagerProgressFragment<IDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";

    public static MembersFragment getInstance(String tag, int teamId) {
        MembersFragment fragment = ADataPagerProgressFragment.getInstance(tag, MembersFragment.class);
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
                    case TeammatesRecyclerAdapter.VIEW_TYPE_HEADER_NEW_MEMBERS:
                    case TeammatesRecyclerAdapter.VIEW_TYPE_HEADER_TEAMMATES:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case TeammatesRecyclerAdapter.VIEW_TYPE_HEADER_NEW_MEMBERS:
                        case TeammatesRecyclerAdapter.VIEW_TYPE_HEADER_TEAMMATES:
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
        return new TeammatesRecyclerAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }
}
