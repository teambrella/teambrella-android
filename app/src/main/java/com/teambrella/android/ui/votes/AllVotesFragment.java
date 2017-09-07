package com.teambrella.android.ui.votes;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * All Votes Fragment
 */
public class AllVotesFragment extends ADataPagerProgressFragment<IAllVoteActivity> {

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        int mode = AllVotesAdapter.MODE_CLAIM;
        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getUri())) {
            case TeambrellaUris.APPLICATION_VOTES:
                mode = AllVotesAdapter.MODE_APPLICATION;
        }
        return new AllVotesAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mode);
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
                    case AllVotesAdapter.VIEW_TYPE_HEADER:
                    case AllVotesAdapter.VIEW_TYPE_ME:
                    case AllVotesAdapter.VIEW_TYPE_BOTTOM:
                    case AllVotesAdapter.VIEW_TYPE_ERROR:
                    case AllVotesAdapter.VIEW_TYPE_LOADING:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case AllVotesAdapter.VIEW_TYPE_HEADER:
                        case AllVotesAdapter.VIEW_TYPE_ME:
                        case AllVotesAdapter.VIEW_TYPE_BOTTOM:
                        case AllVotesAdapter.VIEW_TYPE_ERROR:
                        case AllVotesAdapter.VIEW_TYPE_LOADING:
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
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            Observable.just(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ME))
                    .doOnNext(jsonWrapper -> ((AllVotesAdapter) mAdapter).setMyVote(jsonWrapper))
                    .blockingFirst();
        }
    }
}
