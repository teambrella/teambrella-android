package com.teambrella.android.ui.teammate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.widget.AmountWidget;

import io.reactivex.Notification;

/**
 * Teammate fragment.
 */
public class TeammateFragment extends ATeammateProgressFragment {

    private ImageView mUserPicture;

    private TextView mUserName;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AmountWidget mCoverMe;

    private AmountWidget mCoverThem;

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate, container, false);
        mUserPicture = (ImageView) view.findViewById(R.id.user_picture);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mCoverMe = (AmountWidget) view.findViewById(R.id.cover_me);
        mCoverThem = (AmountWidget) view.findViewById(R.id.cover_them);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);
        if (savedInstanceState == null) {
            mTeammateDataHost.loadTeammate();
            setContentShown(false);
        }
        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        return view;
    }

    private void onRefresh() {
        mTeammateDataHost.loadTeammate();
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject data = notification.getValue().get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            JsonObject basicData = data.get(TeambrellaModel.ATTR_DATA_ONE_BASIC).getAsJsonObject();
            if (basicData != null) {
                Picasso.with(getContext()).load(TeambrellaServer.AUTHORITY + basicData.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                        .into(mUserPicture);
                mCoverMe.setAmount(basicData.get(TeambrellaModel.ATTR_DATA_COVER_ME).getAsFloat());
                mCoverThem.setAmount(basicData.get(TeambrellaModel.ATTR_DATA_COVER_THEM).getAsFloat());
                mUserName.setText(basicData.get(TeambrellaModel.ATTR_DATA_NAME).getAsString());
            }
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
