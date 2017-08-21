package com.teambrella.android.ui;

import com.teambrella.android.data.base.IDataHost;

/**
 * Main Data Host
 */
public interface IMainDataHost extends IDataHost {

    int getTeamId();

    int getTeamType();

    String getTeamName();

    void setProxyPosition(String userId, int position);

    void optInToRating(boolean optIn);

    String getTeamLogoUri();

    void startNewDiscussion();

    void showTeamChooser();

    String getUserId();

    String getCurrency();

    int getTeamAccessLevel();

    boolean isFullTeamAccess();

}
