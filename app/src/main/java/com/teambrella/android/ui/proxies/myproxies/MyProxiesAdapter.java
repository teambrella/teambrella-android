package com.teambrella.android.ui.proxies.myproxies;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import java.util.Locale;

/**
 * My Proxies Adapter
 */

public class MyProxiesAdapter extends TeambrellaDataPagerAdapter {

    private final int mTeamId;
    private final ItemTouchHelper mItemTouchHelper;

    MyProxiesAdapter(IDataPager<JsonArray> pager, int teamId, OnStartActivityListener listener, ItemTouchHelper helper) {
        super(pager, listener);
        mTeamId = teamId;
        mItemTouchHelper = helper;
        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            holder = new MyProxyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_proxy, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof MyProxyViewHolder) {
            setBackground(holder.itemView, position);
            ((MyProxyViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    @Override
    protected RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {
        return new DefaultEmptyViewHolder(parent.getContext(), parent, R.string.my_proxies_empty_prompt, R.drawable.ic_icon_vote);
    }

    @Override
    protected RecyclerView.ViewHolder createBottomViewHolder(ViewGroup parent) {
        return new RecyclerView.ViewHolder(new View(parent.getContext())) {
        };
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        ((MyProxyViewHolder) viewHolder).mPosition.setText(Integer.toString(target.getAdapterPosition() + 1));
        ((MyProxyViewHolder) target).mPosition.setText(Integer.toString(viewHolder.getAdapterPosition() + 1));
        setBackground(viewHolder.itemView, target.getAdapterPosition());
        setBackground(target.itemView, viewHolder.getAdapterPosition());
        super.exchangeItems(viewHolder, target);
    }

    private void setBackground(View itemView, int position) {
        int size = mPager.getLoadedData().size();
        if (size == 1 && position == 0) {
            itemView.setBackgroundResource(R.drawable.section_background);
        } else {
            itemView.setBackgroundResource(position == 0 ? R.drawable.section_background_top : (position == size - 1) ? R.drawable.section_background_bottom :
                    R.drawable.section_background_middle);
        }
    }

    private final class MyProxyViewHolder extends AMemberViewHolder {

        private TextView mLocation;
        private TextView mRank;
        private ProgressBar mDecision;
        private ProgressBar mDiscussion;
        private ProgressBar mVoting;
        private TextView mPosition;

        @SuppressLint("ClickableViewAccessibility")
        MyProxyViewHolder(View itemView) {
            super(itemView, mTeamId);
            mLocation = itemView.findViewById(R.id.location);
            mRank = itemView.findViewById(R.id.rank);
            mDecision = itemView.findViewById(R.id.decision_progress);
            mDiscussion = itemView.findViewById(R.id.discussion_progress);
            mVoting = itemView.findViewById(R.id.voting_progress);
            mPosition = itemView.findViewById(R.id.position);
            itemView.findViewById(R.id.anchor).setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(MyProxyViewHolder.this);
                }
                return false;
            });
        }

        @SuppressLint("SetTextI18n")
        public void onBind(JsonWrapper item) {
            super.onBind(item);
            mRank.setText(String.format(Locale.US, "%.1f", item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)));
            String location = item.getString(TeambrellaModel.ATTR_DATA_LOCATION, "");
            mLocation.setText(location);

            mDecision.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_DECISION_FREQUENCY) * 100));
            mDiscussion.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_DISCUSSION_FREQUENCY) * 100));
            mVoting.setProgress(Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_VOTING_FREQUENCY) * 100));
            mPosition.setText(Integer.toString(getAdapterPosition() + 1));
        }


    }

}
