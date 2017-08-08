package com.teambrella.android.ui.proxies.proxyfor;

import com.google.gson.JsonObject;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;

/**
 * Proxy For Fragment
 */
public class ProxyForFragment extends ADataPagerProgressFragment<IMainDataHost> {
    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ProxyForAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId());
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
    }
}