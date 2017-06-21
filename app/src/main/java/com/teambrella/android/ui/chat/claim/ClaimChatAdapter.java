package com.teambrella.android.ui.chat.claim;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import java.util.Locale;

/**
 * Claim Chat Adapter
 */
class ClaimChatAdapter extends TeambrellaDataPagerAdapter {

    private static final String FORMAT_STRING = "<img src=\"%d\">";
    private static final int VIEW_TYPE_REGULAR_IMAGE = VIEW_TYPE_REGULAR + 1;

    ClaimChatAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }


    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);

        if (viewType == VIEW_TYPE_REGULAR) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            JsonArray images = item.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES);
            String text = item.getString(TeambrellaModel.ATTR_DATA_TEXT);
            if (text != null && images != null && images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        viewType = VIEW_TYPE_REGULAR_IMAGE;
                        break;
                    }
                }
            }
        }

        return viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_REGULAR:
                    viewHolder = new ClaimChatMessageViewHolder(inflater.inflate(R.layout.list_item_message, parent, false));
                    break;
                case VIEW_TYPE_REGULAR_IMAGE:
                    viewHolder = new ClaimChatImageViewHolder(inflater.inflate(R.layout.list_item_message_image, parent, false));
                    break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Picasso picasso = TeambrellaImageLoader.getInstance(holder.itemView.getContext())
                .getPicasso();

        if (holder instanceof ClaimChatMessageViewHolder) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            ((ClaimChatMessageViewHolder) holder).mMessage.setText(Html.fromHtml(item.getString(TeambrellaModel.ATTR_DATA_TEXT)));
            picasso.load(TeambrellaServer.AUTHORITY + item.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART).getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .into(((ClaimChatMessageViewHolder) holder).mUserPicture);

        } else if (holder instanceof ClaimChatImageViewHolder) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            JsonArray images = item.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES);
            String text = item.getString(TeambrellaModel.ATTR_DATA_TEXT);
            if (text != null && images != null && images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        picasso.load(TeambrellaServer.AUTHORITY + images.get(i).getAsString())
                                .into(((ClaimChatImageViewHolder) holder).mImage);
                        break;
                    }
                }
            }
            picasso.load(TeambrellaServer.AUTHORITY + item.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART).getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .into(((ClaimChatImageViewHolder) holder).mUserPicture);
        }
    }

    private static class ClaimChatMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView mUserPicture;
        TextView mMessage;

        ClaimChatMessageViewHolder(View itemView) {
            super(itemView);
            mUserPicture = (ImageView) itemView.findViewById(R.id.user_picture);
            mMessage = (TextView) itemView.findViewById(R.id.message);
        }
    }

    private static class ClaimChatImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mUserPicture;
        ImageView mImage;

        ClaimChatImageViewHolder(View itemView) {
            super(itemView);
            mUserPicture = (ImageView) itemView.findViewById(R.id.user_picture);
            mImage = (ImageView) itemView.findViewById(R.id.image);
        }
    }

}
