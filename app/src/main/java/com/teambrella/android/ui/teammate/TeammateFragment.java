package com.teambrella.android.ui.teammate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.widget.AmountWidget;

import io.reactivex.Notification;

/**
 * Teammate fragment.
 */
public class TeammateFragment extends ADataProgressFragment<IDataHost> {

    private static final String OBJECT_FRAGMENT_TAG = "object_tag";
    private static final String VOTING_STATS_FRAGMENT_TAG = "voting_stats_tag";


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
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }
        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(OBJECT_FRAGMENT_TAG) == null) {
            transaction.add(R.id.object_info_container, ADataFragment.getInstance(mTags, TeammateObjectFragment.class), OBJECT_FRAGMENT_TAG);
        }

        if (fragmentManager.findFragmentByTag(VOTING_STATS_FRAGMENT_TAG) == null) {
            transaction.add(R.id.voting_statistics_container, ADataFragment.getInstance(mTags, TeammateVotingStatsFragment.class), VOTING_STATS_FRAGMENT_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }
    }

    private void onRefresh() {
        mDataHost.load(mTags[0]);
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
