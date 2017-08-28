package com.teambrella.android.ui.chat;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
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
import com.teambrella.android.ui.base.ChatDataPagerAdapter;
import com.teambrella.android.ui.image.ImageViewerActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;
import com.teambrella.android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Claim Chat Adapter
 */
class ChatAdapter extends ChatDataPagerAdapter {

    private static final String FORMAT_STRING = "<img src=\"%d\">";
    private static final int VIEW_TYPE_REGULAR_IMAGE = VIEW_TYPE_REGULAR + 1;

    public static final int MODE_CLAIM = 1;
    public static final int MODE_APPLICATION = 2;
    public static final int MODE_DISCUSSION = 3;

    private final int mTeamId;
    private final int mMode;

    ChatAdapter(IDataPager<JsonArray> pager, int teamId, int mode) {
        super(pager);
        mTeamId = teamId;
        mMode = mode;
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


    private class ClaimChatViewHolder extends RecyclerView.ViewHolder {
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm d LLLL", Locale.ENGLISH);
        ImageView mUserPicture;
        TextView mTime;
        Picasso picasso;

        ClaimChatViewHolder(View itemView) {
            super(itemView);
            picasso = TeambrellaImageLoader.getInstance(itemView.getContext())
                    .getPicasso();
            mUserPicture = itemView.findViewById(R.id.user_picture);
            mTime = itemView.findViewById(R.id.time);
        }

        void bind(JsonWrapper object) {
            Observable.fromArray(object)
                    .map(item -> item.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART))
                    .map(item -> TeambrellaServer.BASE_URL + item.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .map(uri -> picasso.load(uri))
                    .doOnNext(requestCreator -> requestCreator.into(mUserPicture))
                    .map(requestCreator -> true)
                    .subscribe(b -> {
                    }, e -> {
                    }, () -> {
                    });

            mUserPicture.setOnClickListener(v -> {
                String userId = object.getString(TeambrellaModel.ATTR_DATA_USER_ID);
                JsonWrapper teammate = object.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART);
                String name = teammate.getString(TeambrellaModel.ATTR_DATA_NAME);
                String uri = TeambrellaImageLoader.getImageUri(teammate.getString(TeambrellaModel.ATTR_DATA_AVATAR)).toString();
                TeammateActivity.start(itemView.getContext(), mTeamId, userId, name, uri);
            });

            mTime.setText(mDateFormat.format(TimeUtils.getDateFromTicks(object.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0))));
        }
    }


    private class ClaimChatMessageViewHolder extends ClaimChatViewHolder {
        TextView mMessage;
        TextView mTeammateName;
        TextView mVote;

        ClaimChatMessageViewHolder(View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.message);
            mTeammateName = itemView.findViewById(R.id.teammate_name);
            mVote = itemView.findViewById(R.id.vote);
        }

        @Override
        void bind(JsonWrapper object) {
            super.bind(object);
            mMessage.setText(object.getString(TeambrellaModel.ATTR_DATA_TEXT, "").trim());
            JsonWrapper teammate = object.getObject(TeambrellaModel.ATTR_DATA_TEAMMATE_PART);
            String name = teammate != null ? teammate.getString(TeambrellaModel.ATTR_DATA_NAME) : null;
            mTeammateName.setText(name);
            float vote = teammate != null ? teammate.getFloat(TeambrellaModel.ATTR_DATA_VOTE, -1f) : -1f;

            switch (mMode) {
                case MODE_DISCUSSION:
                    mVote.setVisibility(View.GONE);
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
            ArrayList<String> images = TeambrellaModel.getImages(TeambrellaServer.BASE_URL, object.getObject(), TeambrellaModel.ATTR_DATA_IMAGES);
            if (text != null && images != null && images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (text.equals(String.format(Locale.US, FORMAT_STRING, i))) {
                        JsonArray imageRatios = object.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGE_RATIOS);
                        float ratio = imageRatios.get(i).getAsFloat();
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mImage.getLayoutParams();
                        params.dimensionRatio = "" + Math.round(width * ratio) + ":" + width;
                        mImage.setLayoutParams(params);
                        picasso.load(images.get(i))
                                .resize(width, 0)
                                .transform(new MaskTransformation(context, R.drawable.teammate_object_mask))
                                .into(mImage);
                        final int position = i;
                        mImage.setOnClickListener(v -> context.startActivity(ImageViewerActivity.getLaunchIntent(context, images, position)));
                        break;
                    }
                }
            }
        }
    }

}
