package com.teambrella.android.ui.teammate;

import android.support.annotation.StringRes;

import com.teambrella.android.data.base.IDataHost;

/**
 * Teammate Activity
 */
public interface ITeammateActivity extends IDataHost {

    void postVote(double vote);

    void setAsProxy(boolean set);

    boolean isItMe();

    String getCurrency();

    void showSnackBar(@StringRes int text);

    int getTeamId();

    int getTeammateId();
}
