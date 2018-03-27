package com.teambrella.android.ui.team.claims;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Claims fragment
 */
public class TeamClaimsFragment extends AMainDataPagerProgressFragment {

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
                        if (position >= 0) {
                            switch (parent.getAdapter().getItemViewType(position)) {
                                case ClaimsAdapter.VIEW_TYPE_IN_PAYMENT_HEADER:
                                case ClaimsAdapter.VIEW_TYPE_PROCESSED_HEADER:
                                case ClaimsAdapter.VIEW_TYPE_VOTED_HEADER:
                                case ClaimsAdapter.VIEW_TYPE_VOTING_HEADER:
                                case ClaimsAdapter.VIEW_TYPE_VOTED_HEADER_TOP:
                                case ClaimsAdapter.VIEW_TYPE_IN_PAYMENT_HEADER_TOP:
                                case ClaimsAdapter.VIEW_TYPE_PROCESSED_HEADER_TOP:
                                case ClaimsAdapter.VIEW_TYPE_VOTING:
                                case ClaimsAdapter.VIEW_TYPE_BOTTOM:
                                case ClaimsAdapter.VIEW_TYPE_ERROR:
                                case ClaimsAdapter.VIEW_TYPE_LOADING:
                                    drawDivider = false;
                            }
                        }
                        return drawDivider;
                    }
                };
        mList.addItemDecoration(dividerItemDecoration);
    }


    @Override
    public void onStart() {
        super.onStart();
        mObjectDataDisposal = mDataHost.getObservable(MainActivity.USER_DATA).subscribe(this::onUserDataUpdated);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mObjectDataDisposal != null && !mObjectDataDisposal.isDisposed()) {
            mObjectDataDisposal.dispose();
        }
        mObjectDataDisposal = null;
    }


    private void onUserDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            String location = basic.getString(TeambrellaModel.ATTR_DATA_CITY);
            String[] locations = location != null ? location.split(","): null;
            JsonWrapper object = data.getObject(TeambrellaModel.ATTR_DATA_ONE_OBJECT);
            final String objectName = object.getString(TeambrellaModel.ATTR_DATA_MODEL);
            final String objectImageUri = object.getJsonArray(TeambrellaModel.ATTR_DATA_SMALL_PHOTOS).get(0).getAsString();
            ((ClaimsAdapter) mAdapter).setObjectDetails(objectImageUri, objectName, locations != null ? locations[0] : null);
        }
    }


    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID), mDataHost.getCurrency(), mDataHost.isFullTeamAccess(),
                mDataHost::launchActivity);
    }
}
