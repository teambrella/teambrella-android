package com.teambrella.android.ui.team.feed;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.util.TeambrellaDateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.Observable;

/**
 * Feed Adapter
 */
class FeedAdapter extends TeambrellaDataPagerAdapter {


    private static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 1;
    private static final int VIEW_TYPE_ITEM_FEED = VIEW_TYPE_REGULAR + 2;
    private static final int VIEW_TYPE_ITEM_BOTTOM = VIEW_TYPE_REGULAR + 3;


    private final int mTeamId;
    private final IMainDataHost mDataHost;

    FeedAdapter(IMainDataHost dataHost, IDataPager<JsonArray> pager, int teamId, OnStartActivityListener listener) {
        super(pager, listener);
        mTeamId = teamId;
        mDataHost = dataHost;
        setHasStableIds(true);
    }


    static {
        mSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);
        if (viewType == VIEW_TYPE_REGULAR) {
            if (position == 0 && mDataHost.isFullTeamAccess()) {
                return VIEW_TYPE_HEADER;
            } else {
                return VIEW_TYPE_ITEM_FEED;
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
                case VIEW_TYPE_ITEM_FEED:
                    viewHolder = new FeedItemViewHolder(inflater.inflate(R.layout.list_item_feed_claim, parent, false));
                    break;
                case VIEW_TYPE_HEADER:
                    viewHolder = new FeedHeader(inflater.inflate(R.layout.list_item_feed_header, parent, false));
                    break;
                case VIEW_TYPE_ITEM_BOTTOM:
                    viewHolder = new Header(parent, -1, -1, R.drawable.list_item_header_background_bottom);
                    break;


            }
        }
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected int getHeadersCount() {
        return mDataHost.isFullTeamAccess() ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof FeedItemViewHolder) {
            ((FeedItemViewHolder) holder).bind(new JsonWrapper(mPager.getLoadedData().get(position - (mDataHost.isFullTeamAccess() ? 1 : 0)).getAsJsonObject()));
        }
    }

    class FeedItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mTitle;
        private TextView mWhen;
        private TextView mMessage;
        private TeambrellaAvatarsWidgets mAvatarWidgets;
        private TextView mUnread;
        private TextView mType;

        FeedItemViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mTitle = itemView.findViewById(R.id.title);
            mWhen = itemView.findViewById(R.id.when);
            mMessage = itemView.findViewById(R.id.message);
            mAvatarWidgets = itemView.findViewById(R.id.avatars);
            mUnread = itemView.findViewById(R.id.unread);
            mType = itemView.findViewById(R.id.type);
        }

        void bind(JsonWrapper item) {
            int itemType = item.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE);
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            RequestBuilder requestCreator = GlideApp.with(itemView).load(getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)));


            if (itemType == TeambrellaModel.FEED_ITEM_TEAMMATE
                    || itemType == TeambrellaModel.FEED_ITEM_TEAM_CHAT) {
                requestCreator = requestCreator.apply(new RequestOptions().transform(new CircleCrop())
                        .placeholder(R.drawable.picture_background_circle));
            } else {
                requestCreator = requestCreator.apply(new RequestOptions().transforms(new CenterCrop()
                        , new RoundedCorners(context.getResources().getDimensionPixelOffset(R.dimen.rounded_corners_2dp))).placeholder(R.drawable.picture_background_round_2dp));
            }

            requestCreator.into(mIcon);
            String text = item.getString(TeambrellaModel.ATTR_DATA_TEXT, "");
            text = text.replaceAll("<p>", "");
            text = text.replaceAll("</p>", "");
            mMessage.setText(Html.fromHtml(text));


            switch (itemType) {
                case TeambrellaModel.FEED_ITEM_CLAIM:
                    mType.setText(R.string.claim);
                    mType.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_claim), null, null, null);
                    break;
                case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                    mType.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_discussion), null, null, null);
                    mType.setText(R.string.discussion);
                    break;
                default:
                    mType.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_application), null, null, null);
                    mType.setText(R.string.application);
                    break;
            }

            switch (itemType) {
                case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                    mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE));
                    break;
                case TeambrellaModel.FEED_ITEM_TEAMMATE:
                    mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_NAME));
                    break;
                default:
                    mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME));
            }

            int unreadCount = item.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);

            mUnread.setText(item.getString(TeambrellaModel.ATTR_DATA_UNREAD_COUNT));
            mUnread.setVisibility(unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);


            try {
                long time = TeambrellaDateUtils.getServerTime(item.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE));
                long now = System.currentTimeMillis();
                mWhen.setText(DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            int posterCount = item.getInt(TeambrellaModel.ATTR_DATA_POSTER_COUNT);

            Observable.
                    fromIterable(item.getJsonArray(TeambrellaModel.ATTR_DATA_TOP_POSTER_AVATARS))
                    .map(JsonElement::getAsString)
                    .toList()
                    .subscribe(uris -> mAvatarWidgets.setAvatars(getImageLoader(), uris, posterCount));


            itemView.setOnClickListener(v -> {
                switch (itemType) {
                    case TeambrellaModel.FEED_ITEM_CLAIM:
                        startActivity(ChatActivity.getClaimChat(context
                                , mTeamId
                                , item.getInt(TeambrellaModel.ATTR_DATA_ITEM_ID)
                                , item.getString(TeambrellaModel.ATTR_DATA_MODEL_OR_NAME)
                                , item.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)
                                , item.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                , mDataHost.getTeamAccessLevel()
                                , item.getString(TeambrellaModel.ATTR_DATA_ITEM_DATE)));
                        break;
                    case TeambrellaModel.FEED_ITEM_TEAM_CHAT:
                        startActivity(ChatActivity.getFeedChat(context
                                , item.getString(TeambrellaModel.ATTR_DATA_CHAT_TITLE)
                                , item.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                , mDataHost.getTeamId()
                                , mDataHost.getTeamAccessLevel()));
                        break;
                    default:
                        startActivity(ChatActivity.getTeammateChat(context, mTeamId
                                , item.getString(TeambrellaModel.ATTR_DATA_ITEM_USER_ID)
                                , null
                                , item.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO_OR_AVATAR)
                                , item.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID)
                                , mDataHost.getTeamAccessLevel()));
                        break;

                }
            });

        }
    }

    private class FeedHeader extends RecyclerView.ViewHolder {
        FeedHeader(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.start_new_discussion).setOnClickListener(v -> mDataHost.startNewDiscussion());
        }
    }

}
