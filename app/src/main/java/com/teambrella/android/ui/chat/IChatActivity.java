package com.teambrella.android.ui.chat;

import android.net.Uri;

import com.teambrella.android.data.base.IDataHost;

/**
 * Chat Activity
 */
public interface IChatActivity extends IDataHost {
    enum MuteStatus {
        DEFAULT,
        MUTED,
        UMMUTED
    }


    int getTeamId();

    Uri getChatUri();

    int getClaimId();

    String getObjectName();

    String getUserId();

    String getUserName();

    String getImageUri();

    MuteStatus getMuteStatus();

    void setChatMuted(boolean muted);
}
