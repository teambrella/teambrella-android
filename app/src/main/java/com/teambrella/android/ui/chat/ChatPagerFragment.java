package com.teambrella.android.ui.chat;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.TeambrellaUser;

import java.util.Calendar;

/**
 * Clam Chat Pager Fragment
 */
public class ChatPagerFragment extends TeambrellaDataPagerFragment {

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }

    public void addPendingMessage(String postId, String message, float vote) {
        ChatDataPagerLoader loader = (ChatDataPagerLoader) getPager();
        JsonObject post = new JsonObject();
        post.addProperty(TeambrellaModel.ATTR_DATA_USER_ID, TeambrellaUser.get(getContext()).getUserId());
        post.addProperty(TeambrellaModel.ATTR_DATA_TEXT, message);
        post.addProperty(TeambrellaModel.ATTR_DATA_ID, postId);
        post.addProperty(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS, TeambrellaModel.PostStatus.POST_PENDING);
        post.addProperty(TeambrellaModel.ATTR_DATA_ADDED, Calendar.getInstance().getTime().getTime());
        if (vote >= 0) {
            JsonObject teammate = new JsonObject();
            teammate.addProperty(TeambrellaModel.ATTR_DATA_VOTE, vote);
            post.add(TeambrellaModel.ATTR_DATA_ONE_TRAMMATE, teammate);
        }
        loader.addAsNext(post);
    }
}
