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
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.util.TeambrellaDateUtils;

import io.reactivex.Notification;

/**
 * Claims Details Fragment
 */
public class ClaimDetailsFragment extends ADataFragment<IClaimActivity> {


    private TextView mClaimAmount;
    private TextView mExpenses;
    private TextView mDeductible;
    private TextView mCoverage;
    private TextView mIncidentDate;


    public static ClaimDetailsFragment getInstance(String[] dataTags) {
        return ADataFragment.getInstance(dataTags, ClaimDetailsFragment.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_details, container, false);
        mClaimAmount = view.findViewById(R.id.claim_amount);
        mExpenses = view.findViewById(R.id.estimated_expenses);
        mDeductible = view.findViewById(R.id.deductible);
        mCoverage = view.findViewById(R.id.coverage);
        mIncidentDate = view.findViewById(R.id.incident_date);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            if (basic != null) {
                mClaimAmount.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT, 0f))));
                mExpenses.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_ESTIMATED_EXPENSES, 0f))));
                mDeductible.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_DEDUCTIBLE, 0f))));
                mCoverage.setText(getString(R.string.percentage_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_COVERAGE, 0f) * 100)));
                String date = TeambrellaDateUtils.getDatePresentation(getContext()
                        , TeambrellaDateUtils.TEAMBRELLA_UI_DATE
                        , basic.getString(TeambrellaModel.ATTR_DATA_INCIDENT_DATE));
                mDataHost.setSubtitle(date);
                mIncidentDate.setText(date);
            }


        }
    }
}
