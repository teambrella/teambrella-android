package com.teambrella.android.ui.claim;

import com.teambrella.android.data.base.IDataHost;

/**
 * Claim activity interface
 */
public interface IClaimActivity extends IDataHost {

    void setTitle(String title);

    void setSubtitle(String subtitle);

    void postVote(int vote);

}
