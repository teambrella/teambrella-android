package com.teambrella.android.ui.chat;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.TeambrellaUser;

/**
 * Clam Chat Pager Fragment
 */
public class ChatPagerFragment extends TeambrellaDataPagerFragment {

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }

    public void addPendingMessage(String postId, String message) {
        ChatDataPagerLoader loader = (ChatDataPagerLoader) getPager();
        JsonObject post = new JsonObject();
        post.addProperty(TeambrellaModel.ATTR_DATA_USER_ID, TeambrellaUser.get(getContext()).getUserId());
        post.addProperty(TeambrellaModel.ATTR_DATA_TEXT, message);
        post.addProperty(TeambrellaModel.ATTR_DATA_ID, postId);
        loader.addAsNext(post);
    }
}
