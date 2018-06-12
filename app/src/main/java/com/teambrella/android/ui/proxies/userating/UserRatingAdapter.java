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
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import java.util.Locale;

/**
 * User Rating Adapter
 */
public class UserRatingAdapter extends TeambrellaDataPagerAdapter {

    public static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 2;
    public static final int VIEW_TYPE_USER = VIEW_TYPE_REGULAR + 3;
    public static final int VIEW_TYPE_ME = VIEW_TYPE_REGULAR + 4;


    private final int mTeamId;
    private final IMainDataHost mDataHost;

    UserRatingAdapter(IMainDataHost dataHost, IDataPager<JsonArray> pager, int teamId, String currency) {
        super(pager);
        mTeamId = teamId;
        mDataHost = dataHost;
        setHasStableIds(true);
    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_ME;
            case 1:
                return VIEW_TYPE_HEADER;
            default:
                int viewType = super.getItemViewType(position);
                if (viewType == VIEW_TYPE_REGULAR) {
                    viewType = VIEW_TYPE_USER;
                }
                return viewType;
        }
    }

    @Override
    protected int getHeadersCount() {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            switch (viewType) {
                case VIEW_TYPE_ME:
                    return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_me_in_rating, parent, false));
                case VIEW_TYPE_USER:
                    return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false));
                case VIEW_TYPE_HEADER:
                    return new Header(parent, R.string.team_members, R.string.proxy_rank, R.drawable.list_item_header_background_middle);
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof UserViewHolder) {
            position = position != 0 ? position - 1 : position;
            ((UserViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private final class UserViewHolder extends AMemberViewHolder {

        private TextView mRating;
        private TextView mPosition;
        private TextView mOptToRating;
        private TextView mSubtitle;


        UserViewHolder(View itemView) {
            super(itemView, mTeamId);
            mRating = itemView.findViewById(R.id.rating);
            mPosition = itemView.findViewById(R.id.position);
            mOptToRating = itemView.findViewById(R.id.opt_to_rating);
            mSubtitle = itemView.findViewById(R.id.subtitle);
        }

        @SuppressLint("SetTextI18n")
        public void onBind(JsonWrapper item) {
            super.onBind(item);
            int ratingPosition = item.getInt(TeambrellaModel.ATTR_DATA_POSITION, -1);
            float rank = item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK);

            if (rank < 0.005f) {
                rank = 0;
            } else if (rank >= 0.005f && rank < 0.01f) {
                rank = 0.01f;
            }

            mRating.setText(String.format(Locale.US, rank >= 0.1 || rank == 0 ? "%.1f"
                    : "%.2f", rank));
            mPosition.setText(Integer.toString(ratingPosition));
            mSubtitle.setText(item.getString(TeambrellaModel.ATTR_DATA_LOCATION, ""));
            if (mOptToRating != null) {
                mOptToRating.setText(ratingPosition > 0 ? R.string.opt_out_of_rating : R.string.opt_into_rating);
                mOptToRating.setOnClickListener(v -> mDataHost.optInToRating(ratingPosition < 0));
            }
            mPosition.setVisibility(ratingPosition > 0 ? View.VISIBLE : View.INVISIBLE);
        }

    }
}
