package com.teambrella.android.ui.chat;

import android.arch.lifecycle.LiveData;
import android.net.Uri;

import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ITeambrellaDaggerActivity;

import io.reactivex.Notification;

/**
 * Chat Activity
 */
public interface IChatActivity extends IDataHost, ITeambrellaDaggerActivity {
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

    LiveData<Notification<JsonObject>> getPinTopicObservable();

    void pinTopic();

    void unpinTopic();

    void resetPin();
}
