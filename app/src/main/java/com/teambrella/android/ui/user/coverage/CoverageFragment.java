package com.teambrella.android.ui.user.coverage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.teambrella.android.R;

import java.util.Random;

/**
 * Coverage fragment
 */
public class CoverageFragment extends Fragment {

    private TextView mCoverageView;
    private ImageView mCoverageIcon;
    private TextView mMaxExpenses;
    private TextView mPossibleExpenses;
    private TextView mTeamPay;
    private SeekBar mSeekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_coverage, container, false);

        mCoverageView = view.findViewById(R.id.coverage);
        mCoverageIcon = view.findViewById(R.id.coverage_icon);
        mMaxExpenses = view.findViewById(R.id.max_expenses_value);
        mPossibleExpenses = view.findViewById(R.id.possible_expenses_value);
        mTeamPay = view.findViewById(R.id.team_pay_value);
        mSeekBar = view.findViewById(R.id.seek_bar);

        setCoverage(new Random().nextInt(101));
        return view;
    }


    private void setCoverage(int coverage) {
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

    }
}
