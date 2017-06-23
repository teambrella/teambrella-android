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
import com.teambrella.android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get((hasHeader() ? -1 : 0) + position).getAsJsonObject());
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
        if (holder instanceof ClaimChatViewHolder) {
            ((ClaimChatViewHolder) holder).bind(new JsonWrapper(mPager.getLoadedData().get((hasHeader() ? -1 : 0) + position).getAsJsonObject()));
        }
    }


    private static class ClaimChatViewHolder extends RecyclerView.ViewHolder {
        private static SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm d LLLL", Locale.ENGLISH);
        ImageView mUserPicture;
        TextView mTime;
        Picasso picasso;

        ClaimChatViewHolder(View itemView) {
            super(itemView);
            picasso = TeambrellaImageLoader.getInstance(itemView.getContext())
                    .getPicasso();
            mUserPicture = (ImageView) itemView.findViewById(R.id.user_picture);
            mTime = (TextView) itemView.findViewById(R.id.time);
        }

        void bind(JsonWrapper object) {
            picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY,
                    object.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART).getObject(),
                    TeambrellaModel.ATTR_DATA_AVATAR))
                    .into(mUserPicture);
            mTime.setText(mDateFormat.format(TimeUtils.getDateFromTicks(object.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0))));
        }
    }


    private static class ClaimChatMessageViewHolder extends ClaimChatViewHolder {
        TextView mMessage;

        ClaimChatMessageViewHolder(View itemView) {
            super(itemView);
            mMessage = (TextView) itemView.findViewById(R.id.message);
        }

        @Override
        void bind(JsonWrapper object) {
            super.bind(object);
            mMessage.setText(Html.fromHtml(object.getString(TeambrellaModel.ATTR_DATA_TEXT)));
        }
    }

    private static class ClaimChatImageViewHolder extends ClaimChatViewHolder {
        ImageView mImage;

        ClaimChatImageViewHolder(View itemView) {
            super(itemView);
            mImage = (ImageView) itemView.findViewById(R.id.image);
        }

        @Override
        void bind(JsonWrapper object) {
            super.bind(object);
            String text = object.getString(TeambrellaModel.ATTR_DATA_TEXT);
            ArrayList<String> images = TeambrellaModel.getImages(TeambrellaServer.AUTHORITY, object.getObject(), TeambrellaModel.ATTR_DATA_IMAGES);
            if (text != null && images != null && images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        picasso.load(images.get(i)).into(mImage);
                        break;
                    }
                }
            }
        }
    }

}
