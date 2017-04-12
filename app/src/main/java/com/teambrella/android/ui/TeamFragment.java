package com.teambrella.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.teambrella.android.R;

/**
 * Team fragment
 */
public class TeamFragment extends ContentLoadingProgressFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContent(R.layout.fragment_team);
    }
}
