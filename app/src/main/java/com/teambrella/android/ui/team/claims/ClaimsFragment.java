package com.teambrella.android.ui.team.claims;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

/**
 * Claims fragment
 */
public class ClaimsFragment extends ADataPagerProgressFragment<IDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_CURRENCY = "extra_currency";

    public static ClaimsFragment getInstance(String tag, int teamId, String currency) {
        ClaimsFragment fragment = ADataFragmentKt.createDataFragment(new String[]{tag}, ClaimsFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        fragment.getArguments().putString(EXTRA_CURRENCY, currency);
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
                        return drawDivider;
                    }
                };
        getList().addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected ATeambrellaDataPagerAdapter createAdapter() {
        return new ClaimsAdapter(getDataHost().getPager(getTags()[0]), getArguments().getInt(EXTRA_TEAM_ID), getArguments().getString(EXTRA_CURRENCY), false,
                this::startActivity);
    }
}
