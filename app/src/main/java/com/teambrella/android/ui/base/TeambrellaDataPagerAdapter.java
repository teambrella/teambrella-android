package com.teambrella.android.ui.base;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.teammate.TeammateActivityKt;
import com.teambrella.android.util.AmountCurrencyUtil;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teambrella Data Pager Adapter
 */
public class TeambrellaDataPagerAdapter extends ATeambrellaDataPagerAdapter {


    public static final int VIEW_TYPE_LOADING = 1;
    public static final int VIEW_TYPE_ERROR = 2;
    public static final int VIEW_TYPE_BOTTOM = 3;
    public static final int VIEW_TYPE_EMPTY = 4;
    public static final int VIEW_TYPE_REGULAR = 5;
    public static final int VIEW_TYPE_ADD_FUNDS = 6;

    public TeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    public TeambrellaDataPagerAdapter(IDataPager<JsonArray> pager, OnStartActivityListener listener) {
        super(pager, listener);
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        int count = mPager.getLoadedData().size();
        if (count > 0) {
            int size = mPager.getLoadedData().size() + getHeadersCount();
            if (position == size) {
                if (mPager.getHasNextError()) {
                    return VIEW_TYPE_ERROR;
                } else if (mPager.getHasNext()) {
                    return VIEW_TYPE_LOADING;
                } else {
                    return VIEW_TYPE_BOTTOM;
                }
            }
//            JsonArray data = mPager.getLoadedData();
//            JsonObject item = data.get(position).getAsJsonObject();
//            if (item.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE))
            return VIEW_TYPE_REGULAR;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        JsonArray data = mPager.getLoadedData();
        int srcPosition = viewHolder.getAdapterPosition();
        int dstPosition = target.getAdapterPosition();
        JsonElement srcElement = data.get(srcPosition);
        JsonElement dstElement = data.get(dstPosition);
        data.set(srcPosition, dstElement);
        data.set(dstPosition, srcElement);
        notifyItemMoved(srcPosition, dstPosition);
    }

    @Override
    @Nullable
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.list_item_loading, parent, false));
            case VIEW_TYPE_ERROR:
                return new ErrorViewHolder(inflater.inflate(R.layout.list_item_reload, parent, false));
            case VIEW_TYPE_BOTTOM:
                return createBottomViewHolder(parent);
            case VIEW_TYPE_EMPTY:
                return createEmptyViewHolder(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mPager.getHasNext() && !mPager.isNextLoading() && !mPager.getHasNextError() && position > mPager.getLoadedData().size() - 10) {
            mPager.loadNext(false);
        }


        if (holder instanceof ErrorViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                mPager.loadNext(false);
                notifyDataSetChanged();
            });
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    protected int getHeadersCount() {
        return 0;
    }


    @Override
    public int getItemCount() {
        if (mPager.getLoadedData().size() > 0) {
            return mPager.getLoadedData().size() + getHeadersCount() + 1;
        } else {
            return 1;
        }
    }

    protected RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {
        return new DefaultEmptyViewHolder(parent.getContext(), parent, -1, -1);
    }

    protected RecyclerView.ViewHolder createBottomViewHolder(ViewGroup parent) {
        return new Header(parent, -1, -1, R.drawable.list_item_header_background_bottom);
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
        }
    }


    protected static class Header extends RecyclerView.ViewHolder {


        public Header(ViewGroup parent, @StringRes int titleResId, @StringRes int subtitleResId) {
            this(parent, titleResId, subtitleResId, -1);
        }

        public Header(ViewGroup parent, @StringRes int titleResId, @StringRes int subtitleResId, @DrawableRes int backgroundResId) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_header, parent, false));
            TextView titleView = itemView.findViewById(R.id.status_title);
            TextView subtitleView = itemView.findViewById(R.id.status_subtitle);

            if (titleResId != -1)
                titleView.setText(titleResId);

            if (subtitleResId != -1)
                subtitleView.setText(subtitleResId);


            if (backgroundResId != -1) {
                itemView.setBackgroundResource(backgroundResId);
            }
        }

        public Header(ViewGroup parent, String title, String subtitle, @DrawableRes int backgroundResId) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_header, parent, false));
            TextView titleView = itemView.findViewById(R.id.status_title);
            TextView subtitleView = itemView.findViewById(R.id.status_subtitle);
            titleView.setText(title);
            subtitleView.setText(subtitle);


            if (backgroundResId != -1) {
                itemView.setBackgroundResource(backgroundResId);
            }
        }

        public void setTitle(String title) {
            ((TextView) itemView.findViewById(R.id.status_title)).setText(title);
        }

        public void setBackgroundDrawable(@DrawableRes int backgroundResId) {
            if (backgroundResId != -1) {
                itemView.setBackgroundResource(backgroundResId);
            }
        }
    }


    protected abstract class AMemberViewHolder extends RecyclerView.ViewHolder {

        private final int mTeamId;
        private final ImageView mIcon;
        private final TextView mTitle;

        protected AMemberViewHolder(View itemView, int teamId) {
            super(itemView);
            mTeamId = teamId;
            mIcon = itemView.findViewById(R.id.icon);
            mTitle = itemView.findViewById(R.id.title);
        }

        public void onBind(JsonWrapper item) {
            Observable.fromArray(item).map(json -> json.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .map(uri -> getImageLoader().getImageUrl(uri))
                    .subscribe(glideUrl -> GlideApp.with(itemView.getContext()).load(glideUrl)
                            .apply(RequestOptions.downsampleOf(DownsampleStrategy.CENTER_OUTSIDE))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .apply(new RequestOptions().transforms(new CenterCrop()
                                    , new CircleCrop()).placeholder(R.drawable.picture_background_circle)).into(mIcon), throwable -> {
                        // 8)
                    });
            String userPictureUri = Observable.fromArray(item).map(json -> Notification.createOnNext(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .blockingFirst().getValue();
            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            itemView.setOnClickListener(v -> {
                Intent intent = TeammateActivityKt.getTeammateIntent(itemView.getContext(), mTeamId,
                        item.getString(TeambrellaModel.ATTR_DATA_USER_ID), item.getString(TeambrellaModel.ATTR_DATA_NAME), userPictureUri);
                if (!startActivity(intent)) {
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }


    protected static class DefaultEmptyViewHolder extends RecyclerView.ViewHolder {
        public DefaultEmptyViewHolder(Context context, ViewGroup parent, @StringRes int text, @DrawableRes int iconId) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item_empty, parent, false));
            if (text != -1) {
                TextView prompt = itemView.findViewById(R.id.prompt);
                prompt.setText(Html.fromHtml(context.getString(text)));
            }

            if (iconId != -1) {
                ImageView icon = itemView.findViewById(R.id.icon);
                icon.setImageResource(iconId);
            }
        }
    }

    protected class ClaimViewHolder extends RecyclerView.ViewHolder {

        protected final int mTeamId;
        protected final String mCurrency;
        protected ImageView mIcon;
        protected TextView mObject;
        protected TextView mClaimAmount;
        protected TextView mTeammateName;
        protected ImageView mTeammateIcon;
        protected TextView mVote;
        protected ImageView mProxyAvatar;
        protected TextView mProxyName;
        protected ProgressBar mPaymentProgress;
        protected View mViewToVote;
        protected TextView mResultView;
        private NumberFormat mDecimalFormat = DecimalFormat.getInstance();

        private final RequestOptions imageOptions;

        public ClaimViewHolder(View itemView, int teamId, String currency) {
            super(itemView);
            mTeamId = teamId;
            mCurrency = currency;
            mIcon = itemView.findViewById(R.id.icon);
            mObject = itemView.findViewById(R.id.object);
            mClaimAmount = itemView.findViewById(R.id.value);
            mTeammateName = itemView.findViewById(R.id.teammate);
            mTeammateIcon = itemView.findViewById(R.id.teammate_picture);
            mVote = itemView.findViewById(R.id.vote);
            mProxyAvatar = itemView.findViewById(R.id.proxy_picture);
            mProxyName = itemView.findViewById(R.id.proxy);
            mPaymentProgress = itemView.findViewById(R.id.payment_progress);
            mViewToVote = itemView.findViewById(R.id.view_to_vote);
            mResultView = itemView.findViewById(R.id.result);
            imageOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(itemView.getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp)));
        }


        public void onBind(JsonWrapper item) {
            final Context context = itemView.getContext();
            final GlideUrl objectPictureUri = getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO));
            final GlideUrl teammatePictureUri = getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_AVATAR));
            final GlideUrl proxyAvatarUri = getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR));

            RequestManager manager = GlideApp.with(itemView);
            if (mIcon != null) {
                manager.load(objectPictureUri)
                        .apply(imageOptions)
                        .into(mIcon);
            }

            if (teammatePictureUri != null) {
                manager.load(teammatePictureUri).into(mTeammateIcon);
            }

            if (mProxyAvatar != null) {
                if (proxyAvatarUri != null) {
                    manager.load(proxyAvatarUri).into(mProxyAvatar);
                } else {
                    mProxyAvatar.setImageBitmap(null);
                }
            }

            if (mProxyName != null) {
                String proxyName = item.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME);
                if (proxyName != null) {
                    proxyName = proxyName.trim().split(" ")[0];
                }
                mProxyName.setText(proxyName);
            }

            double claimAmount = item.getDouble(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT);
            switch (item.getInt(TeambrellaModel.ATTR_DATA_STATE, -1)) {
                case TeambrellaModel.ClaimStates.VOTING:
                case TeambrellaModel.ClaimStates.VOTED:
                    break;
                case TeambrellaModel.ClaimStates.IN_PAYMENT:
                case TeambrellaModel.ClaimStates.PROCESSEED:
                    mResultView.setText(R.string.claim_reimbursed);
                    mResultView.setTextColor(itemView.getContext().getResources().getColor(R.color.blueGrey));
                    mPaymentProgress.setVisibility(View.VISIBLE);
                    claimAmount *= item.getDouble(TeambrellaModel.ATTR_DATA_REIMBURSEMENT);
                    break;
                case TeambrellaModel.ClaimStates.DECLINED:
                    mResultView.setText(R.string.declined);
                    mResultView.setTextColor(itemView.getContext().getResources().getColor(R.color.blueGrey));
                    mPaymentProgress.setVisibility(View.INVISIBLE);
                    break;
            }

            mObject.setText(item.getString(TeambrellaModel.ATTR_DATA_MODEL));
            mTeammateName.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            mClaimAmount.setText(context.getString(R.string.amount_format_string,
                    AmountCurrencyUtil.getCurrencySign(mCurrency), mDecimalFormat.format(Math.round(claimAmount))));


            if (mVote != null) {
                mVote.setText(itemView.getContext().getString(R.string.claim_vote_format_string, Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE) * 100)));
            }

            if (mPaymentProgress != null) {
                float voting = item.getFloat(TeambrellaModel.ATTR_DATA_VOTING_RES_CRYPTO);
                float payment = item.getFloat(TeambrellaModel.ATTR_DATA_PAYMENT_RES_CRYPTO);
                mPaymentProgress.setProgress(Math.round((payment * 100) / voting));
            }


            if (mViewToVote != null) {
                mViewToVote.setOnClickListener(v -> startActivity(
                        ClaimActivity.Companion.getLaunchIntent(context, item.getInt(TeambrellaModel.ATTR_DATA_ID),
                                item.getString(TeambrellaModel.ATTR_DATA_MODEL), mTeamId)));
            } else {
                itemView.setOnClickListener(v -> {
                    Intent intent = ClaimActivity.Companion.getLaunchIntent(context, item.getInt(TeambrellaModel.ATTR_DATA_ID),
                            item.getString(TeambrellaModel.ATTR_DATA_MODEL), mTeamId);
                    if (!startActivity(intent)) {
                        itemView.getContext().startActivity(intent);
                    }
                });
            }

        }
    }

}
