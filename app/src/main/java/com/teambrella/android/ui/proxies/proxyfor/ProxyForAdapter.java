package com.teambrella.android.ui.proxies.proxyfor;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.TeambrellaDateUtils;

/**
 * Proxy For Adapter
 */
class ProxyForAdapter extends TeambrellaDataPagerAdapter {

    public final static int VIEW_TYPE_COMMISSION = VIEW_TYPE_REGULAR + 1;
    public final static int VIEW_TYPE_TEAMMATES = VIEW_TYPE_REGULAR + 2;
    public final static int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 3;


    private float mTotalCommission = 0f;
    private final int mTeamId;
    private final String mCurrency;

    ProxyForAdapter(IDataPager<JsonArray> pager, int teamId, String currency) {
        super(pager);
        mTeamId = teamId;
        mCurrency = currency;
        setHasStableIds(true);
    }


    void setTotalCommission(float totalCommission) {
        mTotalCommission = totalCommission;
        notifyItemChanged(0);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);


        if (holder == null) {
            switch (viewType) {
                case VIEW_TYPE_COMMISSION:
                    return new CommissionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_commission, parent, false));
                case VIEW_TYPE_HEADER:
                    return new Header(parent, R.string.i_am_proxy_for, -1, R.drawable.list_item_header_background_top);
                case VIEW_TYPE_TEAMMATES:
                    return new ProxyForViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_proxy_for, parent, false));
            }
        }

        return holder;
    }

    @Override
    protected int getHeadersCount() {
        return 2;
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
    protected RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {
        return new DefaultEmptyViewHolder(parent.getContext(), parent, R.string.proxies_for_empty_prompt);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);
        if (viewType == VIEW_TYPE_REGULAR) {
            switch (position) {
                case 0:
                    viewType = VIEW_TYPE_COMMISSION;
                    break;
                case 1:
                    viewType = VIEW_TYPE_HEADER;
                    break;
                default:
                    viewType = VIEW_TYPE_TEAMMATES;
            }
        }
        return viewType;
    }


    private final class CommissionViewHolder extends RecyclerView.ViewHolder {

        private TextView mCommission;

        private CommissionViewHolder(View itemView) {
            super(itemView);
            mCommission = itemView.findViewById(R.id.commission);
        }

        void setCommission(float commission) {
            mCommission.setText(itemView.getContext().getString(R.string.commission_format_string, AmountCurrencyUtil.getCurrencySign(mCurrency), commission));
        }

    }


    private final class ProxyForViewHolder extends AMemberViewHolder {

        private TextView mSubtitle;
        private TextView mCommission;

        ProxyForViewHolder(View itemView) {
            super(itemView, mTeamId);
            mSubtitle = itemView.findViewById(R.id.subtitle);
            mCommission = itemView.findViewById(R.id.commission);
        }

        @Override
        public void onBind(JsonWrapper item) {
            super.onBind(item);
            try {
                String timeString = item.getString(TeambrellaModel.ATTR_DATA_LAST_VOTED);
                if (timeString != null) {
                    long time = TeambrellaDateUtils.getServerTime(timeString);
                    long now = System.currentTimeMillis();
                    mSubtitle.setText(itemView.getContext().getString(R.string.last_voted_format_string
                            , DateUtils.getRelativeTimeSpanString(time, now, DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)));
                } else {
                    mSubtitle.setText(R.string.voting_never);
                }
                mSubtitle.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                mSubtitle.setVisibility(View.INVISIBLE);
            }
            mCommission.setText(itemView.getContext().getString(R.string.commission_format_string, AmountCurrencyUtil.getCurrencySign(mCurrency)
                    , item.getFloat(TeambrellaModel.ATTR_DATA_COMMISSION)));

        }

    }

}
