package com.teambrella.android.ui.chat.claim;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ClaimChatViewHolder) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            ((ClaimChatViewHolder) holder).mMessage.setText(Html.fromHtml(item.getString(TeambrellaModel.ATTR_DATA_TEXT), new Html.ImageGetter() {

                URLDrawable mDrawable = new URLDrawable();

                @Override
                public Drawable getDrawable(String source) {
                    int value = -1;

                    try {
                        value = Integer.parseInt(source);
                    } catch (NumberFormatException e) {

                    }

                    if (value != -1) {
                        TeambrellaImageLoader.getInstance(((ClaimChatViewHolder) holder).mMessage.getContext())
                                .getPicasso().load(TeambrellaServer.AUTHORITY + item.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES).get(value).getAsString())
                                .into((new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        mDrawable.drawable = new BitmapDrawable(bitmap);
                                        // redraw the image by invalidating the container
                                        holder.itemView.invalidate();
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                }));
                    }
                    return mDrawable;
                }
            }, null));
            TeambrellaImageLoader.getInstance(((ClaimChatViewHolder) holder).mMessage.getContext())
                    .getPicasso().load(TeambrellaServer.AUTHORITY + item.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART).getString(TeambrellaModel.ATTR_DATA_AVATAR))
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


    public class URLDrawable extends BitmapDrawable {
        // the drawable that you need to set, you could set the initial drawing
        // with the loading image if you need to
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
