package com.teambrella.android.ui.chat.claim;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * Claim Chat Adapter
 */
class ClaimChatAdapter extends TeambrellaDataPagerAdapter {

    ClaimChatAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_REGULAR:
                    viewHolder = new ClaimChatViewHolder(inflater.inflate(R.layout.list_item_message, parent, false));
                    break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ClaimChatViewHolder) {
            JsonObject item = mPager.getLoadedData().get(position).getAsJsonObject();
            ((ClaimChatViewHolder) holder).mMessage.setText(Html.fromHtml(item.get(TeambrellaModel.ATTR_DATA_TEXT).getAsString()));
            TeambrellaImageLoader.getInstance(((ClaimChatViewHolder) holder).mMessage.getContext())
                    .getPicasso().load(TeambrellaServer.AUTHORITY + item.get(TeambrellaModel.ATTR_DATA_TEMMATE_PART).getAsJsonObject().get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                    .into(((ClaimChatViewHolder) holder).mUserPicture);

        }
    }

    private static class ClaimChatViewHolder extends RecyclerView.ViewHolder {
        ImageView mUserPicture;
        TextView mMessage;

        ClaimChatViewHolder(View itemView) {
            super(itemView);
            mUserPicture = (ImageView) itemView.findViewById(R.id.user_picture);
            mMessage = (TextView) itemView.findViewById(R.id.message);
        }
    }
}
