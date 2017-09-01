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
import android.widget.TextView;

import com.teambrella.android.R;

/**
 * Coverage fragment
 */
public class CoverageFragment extends Fragment {

    private TextView mCoverageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_coverage, container, false);

        mCoverageView = view.findViewById(R.id.coverage);


        SpannableString coverage = new SpannableString("10%");

        coverage.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.darkSkyBlue)), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        coverage.setSpan(new RelativeSizeSpan(0.2f), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mCoverageView.setText(coverage);

        return view;
    }
}
