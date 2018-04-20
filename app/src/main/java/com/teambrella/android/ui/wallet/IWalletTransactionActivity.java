package com.teambrella.android.ui.wallet;

import com.teambrella.android.data.base.IDataHost;

/**
 * Wallet transactions activity interface
 */
public interface IWalletTransactionActivity extends IDataHost {
    int getTeamId();
    String getCurrency();
    Float getCryptoRate();
}
