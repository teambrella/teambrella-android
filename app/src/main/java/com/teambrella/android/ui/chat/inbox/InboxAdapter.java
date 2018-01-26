package com.teambrella.android.ui.chat.inbox;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.util.TeambrellaDateUtils;


/**
 * Inbox Adapter
 */
class InboxAdapter extends TeambrellaDataPagerAdapter {

    InboxAdapter(IDataPager<JsonArray> pager, OnStartActivityListener listener) {
        super(pager, listener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_REGULAR) {
            return new ConversationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation, parent, false));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ConversationViewHolder) {
            ((ConversationViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }


    @Override
    protected RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {
        return new DefaultEmptyViewHolder(parent.getContext(), parent, R.string.no_messages);
    }

    private class ConversationViewHolder extends RecyclerView.ViewHolder {

        private ImageView mUserPicture;
        private TextView mUserName;
        private TextView mWhen;
        private TextView mMessage;
        private TextView mUnreadCount;


        ConversationViewHolder(View itemView) {
            super(itemView);
            mUserPicture = itemView.findViewById(R.id.user_picture);
            mUserName = itemView.findViewById(R.id.user_name);
            mWhen = itemView.findViewById(R.id.when);
            mMessage = itemView.findViewById(R.id.message);
            mUnreadCount = itemView.findViewById(R.id.unread);
        }


        void onBind(JsonWrapper item) {

            String userPictureUri = item.getString(TeambrellaModel.ATTR_DATA_AVATAR);
            if (userPictureUri != null) {
                GlideApp.with(itemView).load(getImageLoader().getImageUrl(userPictureUri))
                        .apply(new RequestOptions().transform(new CircleCrop()))
                        .into(mUserPicture);
            }

            mUserName.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            mMessage.setText(Html.fromHtml(item.getString(TeambrellaModel.ATTR_DATA_TEXT)));
            mUnreadCount.setText(item.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));
            mUnreadCount.setVisibility(item.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT) > 0 ? View.VISIBLE : View.INVISIBLE);
            mWhen.setText(TeambrellaDateUtils.getRelativeTime(-item.getLong(TeambrellaModel.ATTR_DATA_SINCE_LAST_MESSAGE_MINUTES, 0)));

            itemView.setOnClickListener(v -> startActivity(ChatActivity.getConversationChat(itemView.getContext(), item.getString(TeambrellaModel.ATTR_DATA_USER_ID)
                    , item.getString(TeambrellaModel.ATTR_DATA_NAME)
                    , item.getString(TeambrellaModel.ATTR_DATA_AVATAR))));
        }
    }
}
