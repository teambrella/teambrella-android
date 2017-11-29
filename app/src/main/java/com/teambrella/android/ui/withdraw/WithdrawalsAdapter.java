package com.teambrella.android.ui.withdraw;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import java.util.regex.Pattern;

/**
 * Withdrawals Adapter
 */
class WithdrawalsAdapter extends TeambrellaDataPagerAdapter {

    private static final int VIEW_TYPE_SUBMIT_WITHDRAWAL = VIEW_TYPE_REGULAR + 1;

    private String mDefaultWithdrawAddress;

    WithdrawalsAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    public void setDefaultWithdrawAddress(String address) {
        mDefaultWithdrawAddress = address;
        notifyItemChanged(0);
    }

    @Override
    public int getItemViewType(int position) {
//        int viewType = super.getItemViewType(position);
//        if (viewType == VIEW_TYPE_REGULAR) {
        return VIEW_TYPE_SUBMIT_WITHDRAWAL;
//        }
//        return viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            switch (viewType) {
                case VIEW_TYPE_SUBMIT_WITHDRAWAL:
                    return new SubmitWithdrawViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_withdraw_request, parent, false));
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof SubmitWithdrawViewHolder) {
            ((SubmitWithdrawViewHolder) holder).setAddress(mDefaultWithdrawAddress);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class SubmitWithdrawViewHolder extends RecyclerView.ViewHolder {

        private TextView mAddressView;
        private TextView mAmountView;
        private TextView mSubmitView;

        SubmitWithdrawViewHolder(View itemView) {
            super(itemView);
            mAddressView = itemView.findViewById(R.id.eth_address_input);
            mAmountView = itemView.findViewById(R.id.amount_input);
            mSubmitView = itemView.findViewById(R.id.submit);
            mSubmitView.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "" + checkEthereum(mAddressView.getText().toString()), Toast.LENGTH_SHORT).show());
        }

        void setAddress(String address) {
            if (TextUtils.isEmpty(mAddressView.getText())) {
                mAddressView.setText(address);
            }
        }

        private boolean checkEthereum(String address) {
            return Pattern.matches("^0x[a-fA-F0-9]{40}$", address);
        }
    }
}
