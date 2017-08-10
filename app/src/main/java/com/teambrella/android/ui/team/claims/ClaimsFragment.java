package com.teambrella.android.ui.team.claims;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Claims fragment
 */
public class ClaimsFragment extends ADataPagerProgressFragment<IMainDataHost> {

    private static final String EXTRA_TEAM_ID = "extra_team_id";


    private ImageView mObjectIconView;
    private TextView mObjectNameView;
    private TextView mLocationView;
    private String mObjectDataTag;


    private Disposable mObjectDataDisposal;

    public static ClaimsFragment getInstance(String tag, int teamId) {
        ClaimsFragment fragment = ADataPagerProgressFragment.getInstance(tag, ClaimsFragment.class);
        fragment.getArguments().putInt(EXTRA_TEAM_ID, teamId);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mObjectIconView = view.findViewById(R.id.object_icon);
        mObjectNameView = view.findViewById(R.id.title);
        mLocationView = view.findViewById(R.id.subtitle);

        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisiblePosition = ((LinearLayoutManager) (recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition();
                    if (firstVisiblePosition == 0) {
                        ((AppBarLayout) view.findViewById(R.id.appbar)).setExpanded(true, true);
                        setRefreshable(true);
                    } else {
                        setRefreshable(false);
                    }
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mObjectDataDisposal = mDataHost.getObservable(MainActivity.HOME_DATA_TAG).subscribe(this::onObjectDataUpdated);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mObjectDataDisposal != null && !mObjectDataDisposal.isDisposed()) {
            mObjectDataDisposal.dispose();
        }
        mObjectDataDisposal = null;
    }


    private void onObjectDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            final String objectName = data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME);
            mObjectNameView.setText(objectName);
            final String objectImageUri = TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
            picasso.load(objectImageUri).resizeDimen(R.dimen.image_size_48, R.dimen.image_size_48)
                    .centerCrop().
                    transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask)).
                    into(mObjectIconView);
        }
    }


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new ClaimsAdapter(mDataHost.getPager(mTag), getArguments().getInt(EXTRA_TEAM_ID));
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_claims;
    }
}
