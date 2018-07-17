package com.teambrella.android.ui.user.coverage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.QRCodeActivity;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.util.AmountCurrencyUtil;

import io.reactivex.Notification;

/**
 * Coverage fragment
 */
public class CoverageFragment extends ADataFragment<IMainDataHost> {

    private TextView mCoverageView;
    private ImageView mCoverageIcon;
    private TextView mMaxExpenses;
    private TextView mPossibleExpenses;
    private TextView mTeamPay;
    private SeekBar mCoverageSlider;
    private ProgressBar mCoverageProgress;
    private View mFundButton;
    private boolean mIsShown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coverage, container, false);
        mCoverageView = view.findViewById(R.id.coverage);
        mCoverageIcon = view.findViewById(R.id.coverage_icon);
        mMaxExpenses = view.findViewById(R.id.max_expenses_value);
        mPossibleExpenses = view.findViewById(R.id.possible_expenses_value);
        mTeamPay = view.findViewById(R.id.team_pay_value);
        mCoverageSlider = view.findViewById(R.id.coverage_slider);
        mFundButton = view.findViewById(R.id.fund_wallet);
        mCoverageProgress = view.findViewById(R.id.coverage_progress);
        getDataHost().load(getTags()[0]);
        mCoverageSlider.setMax(100);
        mCoverageSlider.setProgress(70);
        return view;
    }


    @Override
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {
        if (notification.isOnNext()) {

            JsonWrapper data = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper coveragePart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_COVERAGE);

            float coverage = coveragePart.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE);
            float limit = coveragePart.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_LIMIT);
            AmountCurrencyUtil.setAmount(mMaxExpenses, Math.round(limit), getDataHost().getCurrency());
            AmountCurrencyUtil.setAmount(mPossibleExpenses, Math.round(limit * 0.7f), getDataHost().getCurrency());
            AmountCurrencyUtil.setAmount(mTeamPay, Math.round(coverage * limit), getDataHost().getCurrency());


            updateCoverageView(coverage);


            mCoverageSlider.setMax(Math.round(limit));
            mCoverageSlider.setProgress(Math.round(limit * 0.7f));
            mCoverageProgress.setMax(mCoverageSlider.getMax());
            mCoverageProgress.setProgress(mCoverageSlider.getProgress());


            mCoverageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    AmountCurrencyUtil.setAmount(mPossibleExpenses, Math.round(i), getDataHost().getCurrency());
                    AmountCurrencyUtil.setAmount(mTeamPay, Math.round(coverage * i), getDataHost().getCurrency());
                    mCoverageProgress.setProgress(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            String fundAddress = getDataHost().getFundAddress();
            if (fundAddress != null) {
                mFundButton.setEnabled(true);
                mFundButton.setOnClickListener(v -> QRCodeActivity.startQRCode(getContext(), fundAddress));
            } else {
                mFundButton.setEnabled(false);
            }

            mIsShown = true;
        } else {
            if (!mIsShown) {
                AmountCurrencyUtil.setAmount(mMaxExpenses, 0, getDataHost().getCurrency());
                AmountCurrencyUtil.setAmount(mPossibleExpenses, 0, getDataHost().getCurrency());
                AmountCurrencyUtil.setAmount(mTeamPay, 0, getDataHost().getCurrency());
                mFundButton.setEnabled(false);
                updateCoverageView(0);
            }
        }
    }

    private void updateCoverageView(float coverage) {
        String coverageString = Integer.toString(Math.round(coverage * 100));
        SpannableString coveragePercent = new SpannableString(coverageString + "%");
        coveragePercent.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.darkSkyBlue)), coverageString.length(), coverageString.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        coveragePercent.setSpan(new RelativeSizeSpan(0.2f), coverageString.length(), coverageString.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mCoverageView.setText(coveragePercent);
        if (coverage > 0.97f) {
            mCoverageIcon.setImageResource(R.drawable.cover_sunny);
        } else if (coverage > 0.90f) {
            mCoverageIcon.setImageResource(R.drawable.cover_lightrain);
        } else {
            mCoverageIcon.setImageResource(R.drawable.cover_rain);
        }
    }
}
