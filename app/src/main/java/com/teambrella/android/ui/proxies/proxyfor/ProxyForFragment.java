package com.teambrella.android.ui.proxies.proxyfor;

import com.google.gson.JsonObject;
import com.teambrella.android.ui.AMainDataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;

/**
 * Proxy For Fragment
 */
public class ProxyForFragment extends AMainDataPagerProgressFragment {
    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        return new ProxyForAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mDataHost.getCurrency());
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
    }
}
