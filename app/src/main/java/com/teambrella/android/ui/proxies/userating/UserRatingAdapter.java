package com.teambrella.android.ui.proxies.userating;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * User Rating Adapter
 */
public class UserRatingAdapter extends TeambrellaDataPagerAdapter {

    private static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 2;
    private static final int VIEW_TYPE_USER = VIEW_TYPE_REGULAR + 3;

    UserRatingAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_HEADER;
            default:
                if (position == getItemCount()) {
                    if (mPager.hasNext() || mPager.isNextLoading()) {
                        return VIEW_TYPE_LOADING;
                    } else {
                        return VIEW_TYPE_ERROR;
                    }
                } else {
                    return VIEW_TYPE_USER;
                }
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            switch (viewType) {
                case VIEW_TYPE_USER:
                    return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false));
                case VIEW_TYPE_HEADER:
                    return new Header(parent, R.string.team_members, R.string.proxy_rank);
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }


    private static final class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mTitle;
        private TextView mRating;
        private TextView mPosition;


        UserViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mRating = (TextView) itemView.findViewById(R.id.rating);
            mPosition = (TextView) itemView.findViewById(R.id.position);
        }

        @SuppressLint("SetTextI18n")
        void onBind(JsonWrapper item) {
            Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .map(uri -> TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso().load(uri))
                    .subscribe(requestCreator -> requestCreator.transform(new CropCircleTransformation()).resize(200, 200).into(mIcon), throwable -> {
                        // 8)
                    });
            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            mRating.setText(itemView.getContext().getString(R.string.risk_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)));
            mPosition.setText(Integer.toString(item.getInt(TeambrellaModel.ATTR_DATA_POSITION, -1)));
        }

    }
}
