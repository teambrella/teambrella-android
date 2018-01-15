package com.teambrella.android.ui.teammates;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teammates Adapter.
 */
class TeammatesByRiskAdapter extends TeambrellaDataPagerAdapter {

    public static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 1;
    public static final int VIEW_TYPE_TEAMMATE = VIEW_TYPE_REGULAR + 2;

    private final int mTeamId;

    /**
     * Constructor
     *
     * @param pager pager
     */
    TeammatesByRiskAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }


    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);

        if (viewType == VIEW_TYPE_REGULAR) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            switch (item.getInt(TeambrellaModel.ATTR_DATA_ITEM_TYPE)) {
                case TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_RISK:
                    return VIEW_TYPE_HEADER;
                case TeambrellaModel.ATTR_DATA_ITEM_TYPE_TEAMMATE:
                    return VIEW_TYPE_TEAMMATE;
            }
        }

        return viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    viewHolder = new Header(parent, -1, R.string.risk, R.drawable.list_item_header_background_middle);
                    break;
                default:
                    viewHolder = new TeammateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_teammate_risk, parent, false));
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof TeammateViewHolder) {
            ((TeammateViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        } else if (holder instanceof Header && getItemViewType(position) == VIEW_TYPE_HEADER) {
            JsonWrapper item = new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject());
            ((Header) holder).setTitle(holder.itemView.getContext().getString(R.string.risk_from_to_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE),
                    item.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE)));
        }
    }

    class TeammateViewHolder extends RecyclerView.ViewHolder {

        ImageView mIconView;
        TextView mTitleView;
        TextView mRiskView;
        TextView mObjectView;

        TeammateViewHolder(View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.icon);
            mTitleView = itemView.findViewById(R.id.title);
            mRiskView = itemView.findViewById(R.id.risk);
            mObjectView = itemView.findViewById(R.id.object);
        }

        void onBind(JsonWrapper item) {
            Observable.fromArray(item).map(json -> json.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                    .map(uri -> getImageLoader().getImageUrl(uri))
                    .subscribe(glideUrl -> Glide.with(itemView).load(glideUrl).apply(new RequestOptions().transform(new CircleCrop())).into(mIconView), throwable -> {
                        // 8)
                    });
            String userPictureUri = Observable.fromArray(item).map(json -> Notification.createOnNext(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .blockingFirst().getValue();
            mTitleView.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            itemView.setOnClickListener(v -> {
                Intent intent = TeammateActivity.getIntent(itemView.getContext(), mTeamId,
                        item.getString(TeambrellaModel.ATTR_DATA_USER_ID), item.getString(TeambrellaModel.ATTR_DATA_NAME), userPictureUri);
                if (!startActivity(intent)) {
                    itemView.getContext().startActivity(intent);
                }
            });

            mRiskView.setText(itemView.getContext().getString(R.string.risk_vote_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_RISK)));
            mObjectView.setText(itemView.getContext().getString(R.string.object_format_string
                    , item.getString(TeambrellaModel.ATTR_DATA_MODEL)
                    , item.getString(TeambrellaModel.ATTR_DATA_YEAR)));
        }
    }

}
