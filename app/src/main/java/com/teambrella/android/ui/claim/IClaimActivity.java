package com.teambrella.android.ui.claim;

import android.support.annotation.StringRes;

import com.teambrella.android.data.base.IDataHost;

/**
 * Claim activity interface
 */
public interface IClaimActivity extends IDataHost {

    void setTitle(String title);

    void setSubtitle(String subtitle);

    void postVote(int vote);

    String getCurrency();


    void showSnackBar(@StringRes int text);

}
