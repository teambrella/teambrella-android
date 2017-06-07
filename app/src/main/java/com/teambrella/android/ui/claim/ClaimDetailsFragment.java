package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;

/**
 * Claims Details Fragment
 */
public class ClaimDetailsFragment extends ADataFragment<IDataHost> {


    private TextView mClaimAmount;
    private TextView mExpenses;
    private TextView mDeductible;
    private TextView mCoverage;
    private TextView mInsidentDate;


    public static ClaimDetailsFragment getInstance(String dataTag) {
        return ADataFragment.getInstance(dataTag, ClaimDetailsFragment.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_details, container, false);
        mClaimAmount = (TextView) view.findViewById(R.id.claim_amount);
        mExpenses = (TextView) view.findViewById(R.id.estimated_expenses);
        mDeductible = (TextView) view.findViewById(R.id.deductible);
        mCoverage = (TextView) view.findViewById(R.id.coverage);
        mInsidentDate = (TextView) view.findViewById(R.id.incident_date);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject response = notification.getValue();
            JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            JsonObject basic = data.get(TeambrellaModel.ATTR_DATA_ONE_BASIC).getAsJsonObject();
            mClaimAmount.setText(getString(R.string.amount_format_string, Math.round(basic.get(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT).getAsDouble())));
            mExpenses.setText(getString(R.string.amount_format_string, Math.round(basic.get(TeambrellaModel.ATTR_DATA_ESTIMATED_EXPENSES).getAsDouble())));
            mDeductible.setText(getString(R.string.amount_format_string, Math.round(basic.get(TeambrellaModel.ATTR_DATA_DETUCTIBLE).getAsDouble())));
            mCoverage.setText(getString(R.string.percentage_format_string, Math.round(basic.get(TeambrellaModel.ATTR_DATA_COVERAGE).getAsDouble() * 100)));
        }
    }
}
