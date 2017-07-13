package com.teambrella.android.ui.team.feed;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
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
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * Feed Adapter
 */
class FeedAdapter extends TeambrellaDataPagerAdapter {


    private static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    private final int mTeamId;

    FeedAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_REGULAR:
                    viewHolder = new FeedItemViewHolder(inflater.inflate(R.layout.list_item_feed_claim, parent, false));
                    break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof FeedItemViewHolder) {
            ((FeedItemViewHolder) holder).bind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    static class FeedItemViewHolder extends RecyclerView.ViewHolder {

        private Picasso picasso;
        private ImageView mIcon;
        private TextView mTitle;
        private TextView mWhen;
        private TextView mMessage;
        private TeambrellaAvatarsWidgets mAvatarWidgets;

        FeedItemViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mWhen = (TextView) itemView.findViewById(R.id.when);
            mMessage = (TextView) itemView.findViewById(R.id.message);
            picasso = TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso();
            mAvatarWidgets = (TeambrellaAvatarsWidgets) itemView.findViewById(R.id.avatars);
        }

        void bind(JsonWrapper item) {
            picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, item.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR))
                    .into(mIcon);
            mMessage.setText(Html.fromHtml(item.getString(TeambrellaModel.ATTR_DATA_TEXT)));

            int itemType = item.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE);

            switch (itemType) {
                case TeambrellaModel.FEED_ITEM_CLAIM:
                    mTitle.setText(itemView.getContext().getString(R.string.claim_title_format_string, item.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)));
                    break;
                default:
                    mTitle.setText(R.string.application);
            }
            try {
                long time = mSDF.parse(item.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE)).getTime();
                long now = System.currentTimeMillis();
                mWhen.setText(DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            Observable.
                    fromIterable(item.getJsonArray(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS))
                    .map(jsonElement -> TeambrellaServer.AUTHORITY + jsonElement.getAsString())
                    .toList()
                    .subscribe(mAvatarWidgets::setAvatars);

        }
    }

}
