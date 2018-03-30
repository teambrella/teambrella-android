package com.teambrella.android.ui.withdraw;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.widget.DividerItemDecoration;

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
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext().getResources().getDrawable(R.drawable.divder)) {

            @Override
            protected boolean canDrawChild(View view, RecyclerView parent) {
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
                    case WithdrawalsAdapter.VIEW_TYPE_EMPTY:
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
                        case WithdrawalsAdapter.VIEW_TYPE_EMPTY:
                            drawDivider = false;
                    }
                }

                return drawDivider;
            }
        };
        mList.addItemDecoration(dividerItemDecoration);
    }

    @SuppressLint("CheckResult")
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
                        adapter.setBalanceValue(jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE)
                                , jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_RESERVED)
                                , mDataHost.getCurrency(), mDataHost.getCurrencyRate());
                    }).blockingFirst();
        }
    }
}
