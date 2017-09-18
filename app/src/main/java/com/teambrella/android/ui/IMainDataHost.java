package com.teambrella.android.ui;

import android.support.annotation.StringRes;

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

    void showSnackBar(@StringRes int text);

    String getFundAddress();

}
