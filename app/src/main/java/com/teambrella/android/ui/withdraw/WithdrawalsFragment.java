package com.teambrella.android.ui.withdraw;

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
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Withdrawals Fragment
 */
public class WithdrawalsFragment extends ADataPagerProgressFragment<IWithdrawActivity> {
    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new WithdrawalsAdapter(mDataHost.getPager(mTag), mDataHost);
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
                    case WithdrawalsAdapter.VIEW_TYPE_HISTORY_HEADER:
                    case WithdrawalsAdapter.VIEW_TYPE_SUBMIT_WITHDRAWAL:
                    case WithdrawalsAdapter.VIEW_TYPE_IN_PROCESS_HEADER:
                    case WithdrawalsAdapter.VIEW_TYPE_QUEUED_HEADER:
                    case WithdrawalsAdapter.VIEW_TYPE_BOTTOM:
                    case WithdrawalsAdapter.VIEW_TYPE_ERROR:
                    case WithdrawalsAdapter.VIEW_TYPE_LOADING:

                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case WithdrawalsAdapter.VIEW_TYPE_HISTORY_HEADER:
                        case WithdrawalsAdapter.VIEW_TYPE_SUBMIT_WITHDRAWAL:
                        case WithdrawalsAdapter.VIEW_TYPE_IN_PROCESS_HEADER:
                        case WithdrawalsAdapter.VIEW_TYPE_QUEUED_HEADER:
                        case WithdrawalsAdapter.VIEW_TYPE_BOTTOM:
                        case WithdrawalsAdapter.VIEW_TYPE_ERROR:
                        case WithdrawalsAdapter.VIEW_TYPE_LOADING:
                            drawDivider = false;
                    }
                }

                if (position != parent.getAdapter().getItemCount() - 1
                        && drawDivider) {
                    super.getItemOffsets(outRect, view, parent, state);
                } else {
                    outRect.setEmpty();
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
                    .doOnNext(jsonWrapper -> {
                        WithdrawalsAdapter adapter = (WithdrawalsAdapter) mAdapter;
                        adapter.setDefaultWithdrawAddress(jsonWrapper.getString(TeambrellaModel.ATTR_DATA_DEFAULT_WITHDRAW_ADDRESS));
                        adapter.setAvailableValue(jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE) - jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_RESERVED));
                    }).blockingFirst();
        }
    }
}
