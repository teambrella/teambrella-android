package com.teambrella.android.ui.withdraw;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Withdrawals Fragment
 */
public class WithdrawalsFragment extends ADataPagerProgressFragment<IDataHost> {
    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new WithdrawalsAdapter(mDataHost.getPager(mTag));
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            Observable.just(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .doOnNext(jsonWrapper ->
                            ((WithdrawalsAdapter) mAdapter).setDefaultWithdrawAddress(jsonWrapper.getString(TeambrellaModel.ATTR_DATA_DEFAULT_WITHDRAW_ADDRESS)))
                    .blockingFirst();
        }
    }
}
