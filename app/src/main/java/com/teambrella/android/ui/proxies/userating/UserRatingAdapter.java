package com.teambrella.android.ui.proxies.userating;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

/**
 * User Rating Adapter
 */
public class UserRatingAdapter extends TeambrellaDataPagerAdapter {

    private static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 2;
    private static final int VIEW_TYPE_USER = VIEW_TYPE_REGULAR + 3;


    private int mTeamId;

    UserRatingAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
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
        return super.getItemCount();
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


    private final class UserViewHolder extends AMemberViewHolder {

        private TextView mRating;
        private TextView mPosition;


        UserViewHolder(View itemView) {
            super(itemView, mTeamId);
            mRating = (TextView) itemView.findViewById(R.id.rating);
            mPosition = (TextView) itemView.findViewById(R.id.position);
        }

        @SuppressLint("SetTextI18n")
        protected void onBind(JsonWrapper item) {
            super.onBind(item);
            mRating.setText(itemView.getContext().getString(R.string.risk_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)));
            mPosition.setText(Integer.toString(item.getInt(TeambrellaModel.ATTR_DATA_POSITION, -1)));
        }

    }
}
