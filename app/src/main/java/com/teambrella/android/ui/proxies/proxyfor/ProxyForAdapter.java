package com.teambrella.android.ui.proxies.proxyfor;

import android.net.Uri;
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
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Proxy For Adapter
 */
class ProxyForAdapter extends TeambrellaDataPagerAdapter {

    private final static int VIEW_TYPE_COMMISSION = VIEW_TYPE_REGULAR + 1;
    private final static int VIEW_TYPE_TEAMMATES = VIEW_TYPE_REGULAR + 2;
    private final static int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 3;


    private float mTotalCommission = 0f;
    private final int mTeamId;

    ProxyForAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }


    void setTotalCommission(float totalCommission) {
        mTotalCommission = totalCommission;
        notifyItemChanged(0);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);


        if (holder == null) {
            switch (viewType) {
                case VIEW_TYPE_COMMISSION:
                    return new CommissionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_commission, parent, false));
                case VIEW_TYPE_HEADER:
                    return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_commission_header, parent, false)) {
                    };
                case VIEW_TYPE_TEAMMATES:
                    return new ProxyForViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_proxy_for, parent, false));
            }
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ProxyForViewHolder) {
            ((ProxyForViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position - 2).getAsJsonObject()));
        } else if (holder instanceof CommissionViewHolder) {
            ((CommissionViewHolder) holder).setCommission(mTotalCommission);
        }
    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_COMMISSION;
            case 1:
                return VIEW_TYPE_HEADER;
            default:
                if (position == getItemCount() - 1) {
                    if (mPager.hasNext() || mPager.isNextLoading()) {
                        return VIEW_TYPE_LOADING;
                    } else {
                        return VIEW_TYPE_ERROR;
                    }
                } else {
                    return VIEW_TYPE_TEAMMATES;
                }
        }
    }


    @Override
    public int getItemCount() {
        return super.getItemCount() + 2;
    }

    private static final class CommissionViewHolder extends RecyclerView.ViewHolder {

        private TextView mCommission;

        private CommissionViewHolder(View itemView) {
            super(itemView);
            mCommission = (TextView) itemView.findViewById(R.id.commission);
        }

        void setCommission(float commission) {
            mCommission.setText(itemView.getContext().getString(R.string.commission_format_string, commission));
        }

    }


    private final class ProxyForViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mTitle;
        private TextView mSubtitle;
        private TextView mCommission;

        ProxyForViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSubtitle = (TextView) itemView.findViewById(R.id.subtitle);
            mCommission = (TextView) itemView.findViewById(R.id.commission);
        }

        void onBind(JsonWrapper item) {
            Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .map(uri -> TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso().load(uri))
                    .subscribe(requestCreator -> requestCreator.transform(new CropCircleTransformation()).resize(200, 200).into(mIcon), throwable -> {
                        // 8)
                    });

            String name = item.getString(TeambrellaModel.ATTR_DATA_NAME);

            mTitle.setText(name);
            mSubtitle.setText(itemView.getContext().getString(R.string.last_voted_format_string, "never"));
            mCommission.setText(itemView.getContext().getString(R.string.commission_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_COMMISSION)));


            Uri teammateUri = TeambrellaUris.getTeammateUri(mTeamId, item.getString(TeambrellaModel.ATTR_DATA_USER_ID));

            itemView.setOnClickListener(v -> itemView.getContext().startActivity(
                    TeammateActivity.getIntent(itemView.getContext()
                            , teammateUri
                            , item.getString(TeambrellaModel.ATTR_DATA_NAME)
                            , TeambrellaImageLoader.getImageUri(item.getString(TeambrellaModel.ATTR_DATA_AVATAR)).toString())
                    )
            );
        }

    }

}
