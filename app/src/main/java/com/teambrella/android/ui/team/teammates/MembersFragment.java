package com.teambrella.android.ui.team.teammates;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.widget.DividerItemDecoration;

import io.reactivex.Notification;

/**
 * Members fragment
 */
public class MembersFragment extends AMainDataPagerProgressFragment {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private boolean mIsShown;

    public static MembersFragment getInstance(String tag, int teamId) {
        MembersFragment fragment = ADataFragmentKt.createDataFragment(new String[]{tag}, MembersFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext().getResources().getDrawable(R.drawable.divder)) {
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
                            case KTeammatesAdapterKt.VIEW_TYPE_HEADER_NEW_MEMBERS:
                            case KTeammatesAdapterKt.VIEW_TYPE_HEADER_TEAMMATES:
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
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            mIsShown = true;
            setContentShown(true);
        } else {
            setContentShown(true, !mIsShown);
            getDataHost().showSnackBar(R.string.something_went_wrong_error);
        }
    }


    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new KTeammateAdapter(getDataHost().getPager(getTags()[0]), getArguments().getInt(EXTRA_TEAM_ID), getDataHost().getCurrency()
                , getDataHost().getInviteFriendsText()
                , getDataHost()::launchActivity);
    }
}
