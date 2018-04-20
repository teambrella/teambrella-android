package com.teambrella.android.ui.votes;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
 * All Votes Adapter
 */
@SuppressWarnings("WeakerAccess")
public class AllVotesAdapter extends TeambrellaDataPagerAdapter {


    public static final int VIEW_TYPE_ME = VIEW_TYPE_REGULAR + 1;
    public static final int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 2;
    public static final int VIEW_TYPE_TEAMMATE = VIEW_TYPE_REGULAR + 3;


    public static final int MODE_CLAIM = 1;
    public static final int MODE_APPLICATION = 2;


    private final int mTeamId;
    private final int mMode;

    private JsonWrapper mMyVote;


    AllVotesAdapter(IDataPager<JsonArray> pager, int teamId, int mode) {
        super(pager);
        mTeamId = teamId;
        mMode = mode;
        setHasStableIds(true);
    }

    public void setMyVote(JsonWrapper vote) {
        mMyVote = vote;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_ME:
                    holder = new MyVoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false));
                    break;
                case VIEW_TYPE_HEADER:
                    holder = new Header(parent, R.string.all_votes, R.string.votes, getHeadersCount() == 2 ? R.drawable.list_item_header_background_middle : R.drawable.list_item_header_background_top);
                    break;
                default:
                    holder = new VoteViewHolder(inflater.inflate(R.layout.list_item_vote, parent, false));
            }
        }
        return holder;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof MyVoteViewHolder) {
            if (mMyVote != null) {
                ((MyVoteViewHolder) holder).onBind(mMyVote);
            }
        } else if (holder instanceof VoteViewHolder) {
            ((VoteViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position - getHeadersCount()).getAsJsonObject()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);
        if (viewType == VIEW_TYPE_REGULAR) {
            switch (position) {
                case 0:
                    return getHeadersCount() == 2 ? VIEW_TYPE_ME :
                            VIEW_TYPE_HEADER;
                case 1:
                    return getHeadersCount() == 2 ? VIEW_TYPE_HEADER :
                            VIEW_TYPE_TEAMMATE;
                default:
                    return VIEW_TYPE_TEAMMATE;
            }
        }
        return viewType;
    }

    @Override
    protected int getHeadersCount() {
        return mMyVote == null || mMyVote.getString(TeambrellaModel.ATTR_DATA_VOTED_BY_PROXY_USER_ID) != null
                || mMyVote.getFloat(TeambrellaModel.ATTR_DATA_VOTE, 0f) <= 0 ? 1 : 2;
    }

    class VoteViewHolder extends AMemberViewHolder {

        private TextView mVoteView;
        private TextView mWeightView;

        VoteViewHolder(View itemView) {
            super(itemView, mTeamId);
            mVoteView = itemView.findViewById(R.id.vote);
            mWeightView = itemView.findViewById(R.id.weight);
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBind(JsonWrapper item) {
            super.onBind(item);
            switch (mMode) {
                case MODE_CLAIM:
                    mVoteView.setText(Html.fromHtml("" + (int) (item.getFloat(TeambrellaModel.ATTR_DATA_VOTE) * 100)) + "%");
                    break;
                case MODE_APPLICATION:
                    mVoteView.setText(itemView.getContext().getString(R.string.risk_vote_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_VOTE)));
                    break;
            }

            float weight = item.getFloat(TeambrellaModel.ATTR_DATA_WEIGHT_COMBINED);
            mWeightView.setText(itemView.getContext().getString(weight >= 0.1 ?
                    R.string.float_format_string_1 : R.string.float_format_string_2, weight));
        }
    }

    public class MyVoteViewHolder extends VoteViewHolder {
        MyVoteViewHolder(View itemView) {
            super(itemView);
        }
    }

}
