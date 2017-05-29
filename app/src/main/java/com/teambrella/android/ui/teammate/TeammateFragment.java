package com.teambrella.android.ui.teammate;

import android.content.Context;
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
import com.teambrella.android.ui.base.ProgressFragment;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Teammate fragment.
 */
public class TeammateFragment extends ProgressFragment {


    private ITeammateDataHost mTeammateDataHost;

    private ImageView mUserPicture;

    private TextView mUserName;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Disposable mDisposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTeammateDataHost = (ITeammateDataHost) context;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate, container, false);
        mUserPicture = (ImageView) view.findViewById(R.id.user_picture);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);
        if (savedInstanceState == null) {
            mTeammateDataHost.loadTeammate();
        }

        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDisposable = mTeammateDataHost.getTeammateObservable().subscribe(this::onResult);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTeammateDataHost = null;
    }

    private void onRefresh() {
        mTeammateDataHost.loadTeammate();
    }


    private void onResult(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject data = notification.getValue().get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            Picasso.with(getContext()).load(TeambrellaServer.AUTHORITY + data.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                    .into(mUserPicture);
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
