package com.teambrella.android.ui.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;

import io.reactivex.Notification;


/**
 * Claim Chat Fragment
 */
public class ChatFragment extends ADataPagerProgressFragment<IChatActivity> {


    long mLastRead = Long.MAX_VALUE;

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        int mode = ChatAdapter.MODE_DISCUSSION;
        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
            case TeambrellaUris.CLAIMS_CHAT:
                mode = ChatAdapter.MODE_CLAIM;
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                mode = ChatAdapter.MODE_APPLICATION;
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                mode = ChatAdapter.MODE_CONVERSATION;
                break;
        }
        return new ChatAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mode, TeambrellaUser.get(getContext()).getUserId());
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRefreshable(false);
        mList.setItemAnimator(null);
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
            if (metadata != null && (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                    || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)) && metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE) > 0) {

                mList.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
                JsonWrapper data = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_DATA).getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
                mLastRead = data.getLong(TeambrellaModel.ATTR_DATA_LAST_READ, Long.MAX_VALUE);
            }

            IDataPager<JsonArray> pager = mDataHost.getPager(mTag);

            int moveTo = pager.getLoadedData().size() - 1;
            for (int i = 0; i < pager.getLoadedData().size(); i++) {
                JsonWrapper item = new JsonWrapper(pager.getLoadedData().get(i).getAsJsonObject());
                long created = item.getLong(TeambrellaModel.ATTR_DATA_CREATED, -1);
                if (created >= mLastRead) {
                    moveTo = i;
                    break;
                }
            }


            LinearLayoutManager manager = (LinearLayoutManager) mList.getLayoutManager();
            manager.scrollToPositionWithOffset(moveTo, 0);
        }

    }
}
