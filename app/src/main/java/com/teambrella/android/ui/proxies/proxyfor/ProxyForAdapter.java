package com.teambrella.android.ui.proxies.proxyfor;

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
import com.teambrella.android.util.AmountCurrencyUtil;

/**
 * Proxy For Adapter
 */
class ProxyForAdapter extends TeambrellaDataPagerAdapter {

    private final static int VIEW_TYPE_COMMISSION = VIEW_TYPE_REGULAR + 1;
    private final static int VIEW_TYPE_TEAMMATES = VIEW_TYPE_REGULAR + 2;
    private final static int VIEW_TYPE_HEADER = VIEW_TYPE_REGULAR + 3;


    private float mTotalCommission = 0f;
    private final int mTeamId;
    private final String mCurrency;

    ProxyForAdapter(IDataPager<JsonArray> pager, int teamId, String currency) {
        super(pager);
        mTeamId = teamId;
        mCurrency = currency;
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
                    } else if (mPager.hasNextError()) {
                        return VIEW_TYPE_ERROR;
                    } else {
                        return VIEW_TYPE_TEAMMATES;
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
            super(itemView, mTeamId, mCurrency);
            mSubtitle = itemView.findViewById(R.id.subtitle);
            mCommission = itemView.findViewById(R.id.commission);
        }

        @Override
        protected void onBind(JsonWrapper item) {
            super.onBind(item);
            mSubtitle.setText(itemView.getContext().getString(R.string.last_voted_format_string, "never"));
            mCommission.setText(itemView.getContext().getString(R.string.commission_format_string, AmountCurrencyUtil.getCurrencySign(mCurrency), item.getFloat(TeambrellaModel.ATTR_DATA_COMMISSION)));

        }

    }

}
