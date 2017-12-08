package com.teambrella.android.ui.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Claim Chat Fragment
 */
public class ChatFragment extends ADataPagerProgressFragment<IChatActivity> {


    long mLastRead = Long.MAX_VALUE;


    private View mVotingPanelView;
    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mIcon;
    private String mUserName;

    @Override
    protected ATeambrellaDataPagerAdapter getAdapter() {
        int mode = ChatAdapter.MODE_DISCUSSION;
        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
            case TeambrellaUris.CLAIMS_CHAT:
                mode = ChatAdapter.MODE_CLAIM;
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                mode = ChatAdapter.MODE_APPLICATION;
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                mode = ChatAdapter.MODE_CONVERSATION;
                break;
        }
        return new ChatAdapter(mDataHost.getPager(mTag), mDataHost.getTeamId(), mode, TeambrellaUser.get(getContext()).getUserId());
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRefreshable(false);
        mList.setBackgroundColor(Color.TRANSPARENT);
        mList.setItemAnimator(null);

        mVotingPanelView = view.findViewById(R.id.voting_panel);
        mTitleView = view.findViewById(R.id.title);
        mSubtitleView = view.findViewById(R.id.subtitle);
        mIcon = view.findViewById(R.id.image);


        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
            case TeambrellaUris.CLAIMS_CHAT:
                mVotingPanelView.setVisibility(View.VISIBLE);
                mVotingPanelView.setOnClickListener(v -> {
                    ClaimActivity.start(getContext(), mDataHost.getClaimId(), mDataHost.getObjectName(), mDataHost.getTeamId());
                    //getActivity().overridePendingTransition(0, 0);
                });
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                mVotingPanelView.setVisibility(View.VISIBLE);
                mVotingPanelView.setOnClickListener(v -> {
                    TeammateActivity.start(getContext(), mDataHost.getTeamId(), mDataHost.getUserId(), mUserName, mDataHost.getImageUri());
                    //getActivity().overridePendingTransition(0, 0);
                });
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                mVotingPanelView.setVisibility(View.GONE);
                mList.setPadding(mList.getPaddingLeft(), 0, mList.getPaddingRight(), mList.getPaddingBottom());
                break;
            case TeambrellaUris.FEED_CHAT:
                mVotingPanelView.setVisibility(View.GONE);
                mList.setPadding(mList.getPaddingLeft(), 0, mList.getPaddingRight(), mList.getPaddingBottom());
                break;

        }

    }


    @Override
    protected int getContentLayout() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
            if (metadata != null && (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                    || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)) && metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE) > 0) {

                mList.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
                JsonWrapper data = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_DATA);
                JsonWrapper discussionPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
                mLastRead = discussionPart.getLong(TeambrellaModel.ATTR_DATA_LAST_READ, Long.MAX_VALUE);


                JsonWrapper basicPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
                if (basicPart != null) {
                    Observable.fromArray(basicPart).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                            .map(uri -> TeambrellaImageLoader.getInstance(getContext()).getPicasso().load(uri))
                            .map(requestCreator -> requestCreator.transform(new CropCircleTransformation()))
                            .subscribe(requestCreator -> requestCreator.into(mIcon), throwable -> {
                                // 8)
                            });
                    mUserName = basicPart.getString(TeambrellaModel.ATTR_DATA_NAME);
                    mTitleView.setText(mUserName);
                    mSubtitleView.setText(getString(R.string.object_format_string
                            , basicPart.getString(TeambrellaModel.ATTR_DATA_MODEL)
                            , basicPart.getString(TeambrellaModel.ATTR_DATA_YEAR)));

                }
            }

            if (mLastRead != -1) {
                IDataPager<JsonArray> pager = mDataHost.getPager(mTag);

                int moveTo = pager.getLoadedData().size() - 1;
                for (int i = 0; i < pager.getLoadedData().size(); i++) {
                    JsonWrapper item = new JsonWrapper(pager.getLoadedData().get(i).getAsJsonObject());
                    long created = item.getLong(TeambrellaModel.ATTR_DATA_CREATED, -1);
                    if (created >= mLastRead) {
                        moveTo = i;
                        break;
                    }
                }
                LinearLayoutManager manager = (LinearLayoutManager) mList.getLayoutManager();
                manager.scrollToPositionWithOffset(moveTo, 0);
                mLastRead = -1;
            }
        }
    }
}
