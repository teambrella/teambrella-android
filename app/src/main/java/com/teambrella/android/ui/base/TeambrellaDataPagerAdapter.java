package com.teambrella.android.ui.base;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Teambrella Data Pager Adapter
 */
public class TeambrellaDataPagerAdapter extends ATeambrellaDataPagerAdapter {


    public static final int VIEW_TYPE_LOADING = 1;

    public static final int VIEW_TYPE_ERROR = 2;

    public static final int VIEW_TYPE_BOTTOM = 3;

    public static final int VIEW_TYPE_EMPTY = 4;

    public static final int VIEW_TYPE_REGULAR = 5;


    public TeambrellaDataPagerAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    public TeambrellaDataPagerAdapter(IDataPager<JsonArray> pager, OnStartActivityListener listener) {
        super(pager, listener);
    }

    @Override
    public int getItemViewType(int position) {
        int count = mPager.getLoadedData().size();
        if (count > 0) {
            int size = mPager.getLoadedData().size() + getHeadersCount();
            if (position == size) {
                if (mPager.hasNextError()) {
                    return VIEW_TYPE_ERROR;
                } else if (mPager.hasNext()) {
                    return VIEW_TYPE_LOADING;
                } else {
                    return VIEW_TYPE_BOTTOM;
                }
            }
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

        if (mPager.hasNext() && !mPager.isNextLoading() && !mPager.hasNextError() && position > mPager.getLoadedData().size() - 10) {
            mPager.loadNext(false);
        }


        if (holder instanceof ErrorViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                mPager.loadNext(false);
                notifyDataSetChanged();
            });
        }
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
        return new DefaultEmptyViewHolder(parent.getContext(), parent, -1);
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

            if (titleResId > 0)
                titleView.setText(titleResId);

            if (subtitleResId > 0)
                subtitleView.setText(subtitleResId);


            if (backgroundResId > 0) {
                itemView.setBackgroundResource(backgroundResId);
            }
        }

        public Header(ViewGroup parent, String title, String subtitle, @DrawableRes int backgroundResId) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_header, parent, false));
            TextView titleView = itemView.findViewById(R.id.status_title);
            TextView subtitleView = itemView.findViewById(R.id.status_subtitle);
            titleView.setText(title);
            subtitleView.setText(subtitle);


            if (backgroundResId > 0) {
                itemView.setBackgroundResource(backgroundResId);
            }
        }

        public void setTitle(String title) {
            ((TextView) itemView.findViewById(R.id.status_title)).setText(title);
        }

        public void setBackgroundDrawable(@DrawableRes int backgroundResId) {
            if (backgroundResId > 0) {
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
            Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .map(uri -> TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso().load(uri))
                    .subscribe(requestCreator -> requestCreator.resize(200, 0).transform(new CropCircleTransformation()).into(mIcon), throwable -> {
                        // 8)
                    });
            String userPictureUri = Observable.fromArray(item).map(json -> Notification.createOnNext(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .blockingFirst().getValue();
            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            itemView.setOnClickListener(v -> {
                Intent intent = TeammateActivity.getIntent(itemView.getContext(), mTeamId,
                        item.getString(TeambrellaModel.ATTR_DATA_USER_ID), item.getString(TeambrellaModel.ATTR_DATA_NAME), userPictureUri);
                if (!startActivity(intent)) {
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }


    protected static class DefaultEmptyViewHolder extends RecyclerView.ViewHolder {
        public DefaultEmptyViewHolder(Context context, ViewGroup parent, @StringRes int text) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item_empty, parent, false));
            if (text > 0) {
                TextView prompt = itemView.findViewById(R.id.prompt);
                prompt.setText(Html.fromHtml(context.getString(text)));
            }
        }
    }
}
