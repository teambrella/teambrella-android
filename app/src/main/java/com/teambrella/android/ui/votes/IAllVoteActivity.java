package com.teambrella.android.ui.votes;

import android.net.Uri;

import com.teambrella.android.data.base.IDataHost;

/**
 * All Vote Activity Interface
 */
public interface IAllVoteActivity extends IDataHost {

    int getTeamId();

    Uri getUri();

}
