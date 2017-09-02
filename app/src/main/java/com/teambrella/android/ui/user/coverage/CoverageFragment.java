package com.teambrella.android.ui.user.coverage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.util.AmountCurrencyUtil;

import java.util.Random;

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
    private boolean mIsShown;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coverage, container, false);
        mCoverageView = view.findViewById(R.id.coverage);
        mCoverageIcon = view.findViewById(R.id.coverage_icon);
        mMaxExpenses = view.findViewById(R.id.max_expenses_value);
        mPossibleExpenses = view.findViewById(R.id.possible_expenses_value);
        mTeamPay = view.findViewById(R.id.team_pay_value);
        mCoverageSlider = view.findViewById(R.id.coverage_slider);
        return view;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            AmountCurrencyUtil.setAmount(mMaxExpenses, 1200, mDataHost.getCurrency());
            AmountCurrencyUtil.setAmount(mPossibleExpenses, 600, mDataHost.getCurrency());
            AmountCurrencyUtil.setAmount(mTeamPay, 200, mDataHost.getCurrency());

            final int coverage = new Random().nextInt(101);
            String coverageString = Integer.toString(coverage);
            SpannableString coveragePercent = new SpannableString(coverage + "%");
            coveragePercent.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.darkSkyBlue)), coverageString.length(), coverageString.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            coveragePercent.setSpan(new RelativeSizeSpan(0.2f), coverageString.length(), coverageString.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mCoverageView.setText(coveragePercent);

            if (coverage > 97) {
                mCoverageIcon.setImageResource(R.drawable.cover_sunny);
            } else if (coverage > 90) {
                mCoverageIcon.setImageResource(R.drawable.cover_lightrain);
            } else {
                mCoverageIcon.setImageResource(R.drawable.cover_rain);
            }

            mCoverageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mIsShown = true;
        }
    }
}
