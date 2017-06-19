package com.teambrella.android.ui.chat.claim;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.TeambrellaChatDataPagerLoader;

/**
 * Claim chat pager loader
 */
class ClaimChatDataPagerLoader extends TeambrellaChatDataPagerLoader {

    ClaimChatDataPagerLoader(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return src.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_CHAT).getAsJsonArray();
    }
}
