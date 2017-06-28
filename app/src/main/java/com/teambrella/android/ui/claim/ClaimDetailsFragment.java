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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.Notification;

/**
 * Claims Details Fragment
 */
public class ClaimDetailsFragment extends ADataFragment<IClaimActivity> {


    private TextView mClaimAmount;
    private TextView mExpenses;
    private TextView mDeductible;
    private TextView mCoverage;
    private TextView mInsidentDate;
    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("d LLLL", Locale.ENGLISH);
    private static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    public static ClaimDetailsFragment getInstance(String[] dataTags) {
        return ADataFragment.getInstance(dataTags, ClaimDetailsFragment.class);
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
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            if (basic != null) {
                mClaimAmount.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT, 0f))));
                mExpenses.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_ESTIMATED_EXPENSES, 0f))));
                mDeductible.setText(getString(R.string.amount_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_DEDUCTIBLE, 0f))));
                mCoverage.setText(getString(R.string.percentage_format_string, Math.round(basic.getDouble(TeambrellaModel.ATTR_DATA_COVERAGE, 0f) * 100)));
                try {
                    mDataHost.setSubtitle(mDateFormat.format(mSDF.parse(basic.getString(TeambrellaModel.ATTR_DATA_INCIDENT_DATE))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
