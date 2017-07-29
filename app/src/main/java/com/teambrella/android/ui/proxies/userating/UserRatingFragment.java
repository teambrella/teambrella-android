package com.teambrella.android.ui.proxies.userating;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * User Rating Fragment
 */
public class UserRatingFragment extends ADataPagerProgressFragment<IMainDataHost> {

    private ImageView mUserAvatar;
    private TextView mUserName;
    private TextView mRating;
    private TextView mOptToRating;
    private TextView mPosition;


    @Override
    protected TeambrellaDataPagerAdapter getAdapter() {
        return new UserRatingAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId());
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.divder));
        mList.addItemDecoration(dividerItemDecoration);

        mUserAvatar = (ImageView) view.findViewById(R.id.icon);
        mUserName = (TextView) view.findViewById(R.id.title);
        mRating = (TextView) view.findViewById(R.id.rating);
        mOptToRating = (TextView) view.findViewById(R.id.opt_to_rating);
        mPosition = (TextView) view.findViewById(R.id.position);


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

        IDataPager<JsonArray> mPager = mDataHost.getPager(mTag);
        JsonArray data = mPager.getLoadedData();
        if (data != null && data.size() > 0) {
            updateHeader(new JsonWrapper(data.get(0).getAsJsonObject()));
        }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_user_ratings_list;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            Observable.just(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .map(jsonWrapper -> jsonWrapper.getArray(TeambrellaModel.ATTR_DATA_MEMBERS))
                    .map(jsonWrappers -> jsonWrappers.get(0))
                    .doOnNext(this::updateHeader).onErrorReturnItem(new JsonWrapper(null)).blockingFirst();
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateHeader(JsonWrapper item) {
        Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                .map(uri -> TeambrellaImageLoader.getInstance(getContext()).getPicasso().load(uri))
                .subscribe(requestCreator -> requestCreator.transform(new CropCircleTransformation()).resize(200, 200).into(mUserAvatar), throwable -> {
                    // 8)
                });
        mUserName.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
        mRating.setText(getContext().getString(R.string.risk_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_PROXY_RANK)));
        mOptToRating.setText(item.getInt(TeambrellaModel.ATTR_DATA_POSITION, -1) > 0 ? R.string.opt_out_of_rating : R.string.opt_into_rating);
        int position = item.getInt(TeambrellaModel.ATTR_DATA_POSITION, -1);
        mPosition.setVisibility(position >= 0 ? View.VISIBLE : View.INVISIBLE);
        mPosition.setText(Integer.toString(position));

        mOptToRating.setOnClickListener(v -> {
            mDataHost.optInToRating(position < 0);
            setRefreshing(true);
        });

    }
}
