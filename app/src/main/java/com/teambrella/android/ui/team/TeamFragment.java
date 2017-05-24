package com.teambrella.android.ui.team;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambrella.android.R;

/**
 * Created by dvasilin on 24/05/2017.
 */

public class TeamFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stub, container, false);
        ((TextView) view.findViewById(R.id.message)).setText(R.string.bottom_navigation_team);
        return view;
    }
}
