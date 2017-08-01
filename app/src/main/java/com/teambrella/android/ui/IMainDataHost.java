package com.teambrella.android.ui;

import com.teambrella.android.data.base.IDataHost;

/**
 * Main Data Host
 */
public interface IMainDataHost extends IDataHost {

    int getTeamId();

    void setProxyPosition(String userId, int position);

    void optInToRating(boolean optIn);

    String getTeamLogoUri();
}
