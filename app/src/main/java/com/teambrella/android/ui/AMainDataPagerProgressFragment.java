package com.teambrella.android.ui;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.util.ConnectivityUtils;

import io.reactivex.Notification;

/**
 * Base Main Data Pager Progress Fragment
 */
public abstract class AMainDataPagerProgressFragment extends ADataPagerProgressFragment<IMainDataHost> {

    private boolean mIsShown;

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            mIsShown = true;
            setContentShown(true);
        } else {
            setContentShown(true, !mIsShown);
            mDataHost.showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext()) ? R.string.something_went_wrong_error : R.string.no_internet_connection);
        }
    }
}
