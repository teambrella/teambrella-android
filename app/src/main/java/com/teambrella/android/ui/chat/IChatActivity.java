package com.teambrella.android.ui.chat;

import android.net.Uri;

import com.teambrella.android.data.base.IDataHost;

/**
 * Chat Activity
 */
public interface IChatActivity extends IDataHost {

    int getTeamId();

    Uri getChatUri();
}
