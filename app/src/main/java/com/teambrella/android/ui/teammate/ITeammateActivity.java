package com.teambrella.android.ui.teammate;

import com.teambrella.android.data.base.IDataHost;

/**
 * Teammate Activity
 */
public interface ITeammateActivity extends IDataHost {

    void postVote(double vote);

    void setAsProxy(boolean set);

    boolean isItMe();

    String getCurrency();
}
