package com.teambrella.android.ui.team.claims;

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
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Claims fragment
 */
public class TeamClaimsFragment extends ADataPagerProgressFragment<IMainDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";


    private Disposable mObjectDataDisposal;

    public static TeamClaimsFragment getInstance(String tag, int teamId, String currency) {
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
                    case ClaimsAdapter.VIEW_TYPE_VOTING:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case ClaimsAdapter.VIEW_TYPE_IN_PAYMENT_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_PROCESSED_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_VOTED_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_VOTING_HEADER:
                        case ClaimsAdapter.VIEW_TYPE_VOTING:
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
    public void onStart() {
        super.onStart();
        mObjectDataDisposal = mDataHost.getObservable(MainActivity.HOME_DATA_TAG).subscribe(this::onObjectDataUpdated);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mObjectDataDisposal != null && !mObjectDataDisposal.isDisposed()) {
            mObjectDataDisposal.dispose();
        }
        mObjectDataDisposal = null;
    }


    private void onObjectDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            final String objectName = data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME);
            final String objectImageUri = TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
            ((ClaimsAdapter) mAdapter).setObjectDetails(objectImageUri, objectName, null);
        }
    }


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID), mDataHost.getCurrency(), mDataHost.isFullTeamAccess());
    }
}
