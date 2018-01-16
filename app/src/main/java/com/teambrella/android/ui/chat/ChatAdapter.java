package com.teambrella.android.ui.chat;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ChatDataPagerAdapter;
import com.teambrella.android.ui.image.ImageViewerActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;
import com.teambrella.android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * Claim Chat Adapter
 */
class ChatAdapter extends ChatDataPagerAdapter {

    private static final String FORMAT_STRING = "<img src=\"%d\">";
    private static final int VIEW_TYPE_MESSAGE_ME = VIEW_TYPE_REGULAR + 1;
    private static final int VIEW_TYPE_MESSAGE_THEM = VIEW_TYPE_REGULAR + 2;
    private static final int VIEW_TYPE_IMAGE_ME = VIEW_TYPE_REGULAR + 3;
    private static final int VIEW_TYPE_IMAGE_THEM = VIEW_TYPE_REGULAR + 4;

    static final int MODE_CLAIM = 1;
    static final int MODE_APPLICATION = 2;
    static final int MODE_DISCUSSION = 3;
    static final int MODE_CONVERSATION = 4;

    private final int mTeamId;
    private final int mMode;
    private final String mUserId;

    ChatAdapter(IDataPager<JsonArray> pager, int teamId, int mode, String userId) {
        super(pager);
        mTeamId = teamId;
        mMode = mode;
        mUserId = userId;
    }


    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);

        if (viewType == VIEW_TYPE_REGULAR) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get((hasHeader() ? -1 : 0) + position).getAsJsonObject());
            boolean isItMine = mUserId.equals(item.getString(TeambrellaModel.ATTR_DATA_USER_ID));
            JsonArray images = item.getJsonArray(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES);
            if (images == null) {
                images = item.getJsonArray(TeambrellaModel.ATTR_DATA_SMALL_IMAGES);
            }
            String text = item.getString(TeambrellaModel.ATTR_DATA_TEXT);
            if (text != null && images != null && images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        viewType = isItMine ? VIEW_TYPE_IMAGE_ME : VIEW_TYPE_IMAGE_THEM;
                        break;
                    }
                }
            }

            if (viewType == VIEW_TYPE_REGULAR) {
                viewType = isItMine ? VIEW_TYPE_MESSAGE_ME : VIEW_TYPE_MESSAGE_THEM;
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
                case VIEW_TYPE_MESSAGE_THEM:
                    viewHolder = new ClaimChatMessageViewHolder(inflater.inflate(R.layout.list_item_message, parent, false));
                    break;
                case VIEW_TYPE_MESSAGE_ME:
                    viewHolder = new ClaimChatMessageViewHolder(inflater.inflate(R.layout.list_item_my_message, parent, false));
                    break;
                case VIEW_TYPE_IMAGE_THEM:
                    viewHolder = new ClaimChatImageViewHolder(inflater.inflate(R.layout.list_item_message_image, parent, false));
                    break;
                case VIEW_TYPE_IMAGE_ME:
                    viewHolder = new ClaimChatImageViewHolder(inflater.inflate(R.layout.list_item_my_message_image, parent, false));
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

    @Override
    protected RecyclerView.ViewHolder createErrorViewHolder(ViewGroup parent) {
        return new RecyclerView.ViewHolder(new View(parent.getContext())) {
        };
    }


    @Override
    protected RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
        return new RecyclerView.ViewHolder(new View(parent.getContext())) {
        };
    }

    private class ClaimChatViewHolder extends RecyclerView.ViewHolder {
        private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("d LLLL", Locale.ENGLISH);
        ImageView mUserPicture;
        TextView mTime;
        TextView mDate;
        TeambrellaImageLoader imageLoader;
        View mStatus;

        ClaimChatViewHolder(View itemView) {
            super(itemView);
            imageLoader = getImageLoader();
            mUserPicture = itemView.findViewById(R.id.user_picture);
            mTime = itemView.findViewById(R.id.time);
            mDate = itemView.findViewById(R.id.date);
            mStatus = itemView.findViewById(R.id.status);
        }

        void bind(JsonWrapper object) {

            if (mUserPicture != null) {
                Observable.fromArray(object)
                        .map(item -> item.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART))
                        .map(item -> item.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                        .map(item -> imageLoader.getImageUrl(item))
                        .map(uri -> GlideApp.with(itemView).load(uri))
                        .doOnNext(drawableRequestBuilder -> drawableRequestBuilder.apply(new RequestOptions()
                                .transform(new CircleCrop())).into(mUserPicture))
                        .subscribe(b -> {
                        }, e -> {
                        }, () -> {
                        });

                mUserPicture.setOnClickListener(v -> {
                    if (mTeamId > 0) {
                        String userId = object.getString(TeambrellaModel.ATTR_DATA_USER_ID);
                        JsonWrapper teammate = object.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART);
                        String name = teammate.getString(TeambrellaModel.ATTR_DATA_NAME);
                        String uri = TeambrellaImageLoader.getImageUri(teammate.getString(TeambrellaModel.ATTR_DATA_AVATAR)).toString();
                        TeammateActivity.start(itemView.getContext(), mTeamId, userId, name, uri);
                    }
                });
            }

            mTime.setText(mTimeFormat.format(TimeUtils.getDateFromTicks(object.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0))));
            if (TeambrellaModel.PostStatus.POST_PENDING.equals(object.getString(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS))) {
                mDate.setText(mDateFormat.format(new Date(object.getLong(TeambrellaModel.ATTR_DATA_ADDED, 0))));
            } else {
                mDate.setText(mDateFormat.format(TimeUtils.getDateFromTicks(object.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0))));
            }

            mDate.setVisibility(object.getBoolean(TeambrellaModel.ATTR_DATA_IS_NEXT_DAY, false) ? View.VISIBLE : View.GONE);

            if (mStatus != null) {
                mStatus.setVisibility(TeambrellaModel.PostStatus.POST_PENDING.equals(object.getString(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS))
                        ? View.VISIBLE : View.GONE);
            }
            mTime.setVisibility(mStatus == null || mStatus.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        }
    }


    private class ClaimChatMessageViewHolder extends ClaimChatViewHolder {
        TextView mMessage;
        TextView mTeammateName;
        TextView mVote;
        View mHeader;

        ClaimChatMessageViewHolder(View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.message);
            mMessage.setMovementMethod(LinkMovementMethod.getInstance());
            mMessage.setLinksClickable(true);
            mTeammateName = itemView.findViewById(R.id.teammate_name);
            mVote = itemView.findViewById(R.id.vote);
            mHeader = itemView.findViewById(R.id.header);
        }

        @Override
        void bind(JsonWrapper object) {
            super.bind(object);
            mMessage.setText(Html.fromHtml(object.getString(TeambrellaModel.ATTR_DATA_TEXT, "").trim()));
            JsonWrapper teammate = object.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART);
            String name = teammate != null ? teammate.getString(TeambrellaModel.ATTR_DATA_NAME) : null;

            if (mTeammateName != null) {
                mTeammateName.setText(name);
            }

            float vote = teammate != null ? teammate.getFloat(TeambrellaModel.ATTR_DATA_VOTE, -1f) : -1f;

            switch (mMode) {
                case MODE_DISCUSSION:
                    mVote.setVisibility(View.INVISIBLE);
                    break;
                case MODE_CLAIM:
                    mVote.setVisibility(View.VISIBLE);
                    if (vote > 0) {
                        mVote.setText(itemView.getContext().getString(R.string.claim_chat_vote, Math.round(vote * 100)));
                    } else {
                        mVote.setText(R.string.chat_not_voted_yet);
                    }
                    break;
                case MODE_APPLICATION:
                    mVote.setVisibility(View.VISIBLE);
                    if (vote > 0) {
                        mVote.setText(itemView.getContext().getString(R.string.application_chat_vote, vote));
                    } else {
                        mVote.setText(R.string.chat_not_voted_yet);
                    }
                    break;
                case MODE_CONVERSATION:
                    mVote.setVisibility(View.INVISIBLE);
                    if (mUserPicture != null) {
                        mUserPicture.setVisibility(View.GONE);
                    }
                    if (mHeader != null) {
                        mHeader.setVisibility(View.GONE);
                    }
                    break;
            }

        }
    }

    private class ClaimChatImageViewHolder extends ClaimChatViewHolder {
        ImageView mImage;
        final int width;

        ClaimChatImageViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.image);
            width = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.chat_image_width);
        }

        @Override
        void bind(JsonWrapper object) {
            super.bind(object);

            Context context = itemView.getContext();
            String text = object.getString(TeambrellaModel.ATTR_DATA_TEXT);

            ArrayList<String> smallImages;
            JsonArray localImages = object.getJsonArray(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES);
            if (localImages != null && localImages.size() > 0) {
                smallImages = new ArrayList<>();
                for (JsonElement item : localImages) {
                    smallImages.add("file:" + item.getAsString());
                }
            } else {
                smallImages = TeambrellaModel.getImages("", object.getObject(), TeambrellaModel.ATTR_DATA_SMALL_IMAGES);
            }

            if (text != null && smallImages != null && smallImages.size() > 0) {
                for (int i = 0; i < smallImages.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        JsonArray imageRatios = object.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGE_RATIOS);
                        float ratio = imageRatios.get(i).getAsFloat();
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mImage.getLayoutParams();
                        params.dimensionRatio = "" + Math.round(width * ratio) + ":" + width;
                        mImage.setLayoutParams(params);
                        String smallImage = smallImages.get(i);
                        if (smallImage.startsWith("file:")) {
                            GlideApp.with(itemView).load(smallImage)
                                    .apply(new RequestOptions().transform(new RoundedCorners(itemView.getResources()
                                            .getDimensionPixelOffset(R.dimen.rounded_corners_4dp)))).into(mImage);
                        } else {
                            GlideApp.with(itemView).load(getImageLoader().getImageUrl(smallImage))
                                    .apply(new RequestOptions().transform(new RoundedCorners(itemView.getResources()
                                            .getDimensionPixelOffset(R.dimen.rounded_corners_4dp)))).into(mImage);
                        }
                        final int position = i;
                        if (!TeambrellaModel.PostStatus.POST_PENDING.equals(object.getString(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS))) {
                            ArrayList<String> images = TeambrellaModel.getImages("", object.getObject(), TeambrellaModel.ATTR_DATA_IMAGES);
                            mImage.setOnClickListener(v -> context.startActivity(ImageViewerActivity.getLaunchIntent(context, images, position)));
                        }
                        break;
                    }
                }
            }
        }
    }

}
