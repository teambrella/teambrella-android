package com.teambrella.android.ui.withdraw;

import com.teambrella.android.data.base.IDataHost;

/**
 * Interface for Withdraw Activity
 */
public interface IWithdrawActivity extends IDataHost {
    void showWithdrawInfo();

    void requestWithdraw(String address, float amount);
}
