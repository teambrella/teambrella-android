package com.teambrella.android.ui.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.claim.ClaimVotingFragment;
import com.teambrella.android.ui.teammate.TeammateActivity;

import java.util.Locale;

import io.reactivex.Notification;
import io.reactivex.Observable;


/**
 * Claim Chat Fragment
 */
public class ChatFragment extends ADataPagerProgressFragment<IChatActivity> {

    private static final String VOTING_FRAGMENT_TAG = "voting_fragment_tag";


    Long mLastRead = null;


    @SuppressWarnings("FieldCanBeLocal")
    private View mVotingPanelView;
    private TextView mTitleView;
    private TextView mSubtitleView;
    private TextView mVoteValueView;
    private TextView mVoteTitleView;
    private View mVoteButton;
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
        ((LinearLayoutManager) mList.getLayoutManager()).setStackFromEnd(true);

        mVotingPanelView = view.findViewById(R.id.voting_panel);
        mTitleView = view.findViewById(R.id.title);
        mSubtitleView = view.findViewById(R.id.subtitle);
        mVoteValueView = view.findViewById(R.id.vote_value);
        mVoteButton = view.findViewById(R.id.vote);
        mIcon = view.findViewById(R.id.image);
        mVoteTitleView = view.findViewById(R.id.your_vote_title);


        switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
            case TeambrellaUris.CLAIMS_CHAT:
                mVotingPanelView.setVisibility(View.VISIBLE);
                mVotingPanelView.setOnClickListener(this::onClaimClickListener);
                mVoteButton.setOnClickListener(this::onClaimClickListener);
                FragmentManager fragmentManager = getChildFragmentManager();
                if (fragmentManager.findFragmentByTag(VOTING_FRAGMENT_TAG) == null) {
                    fragmentManager.beginTransaction().add(R.id.voting_container,
                            ADataFragment.getInstance(new String[]{ChatActivity.CLAIM_DATA_TAG, ChatActivity.VOTE_DATA_TAG}
                                    , ClaimVotingFragment.class)
                            , VOTING_FRAGMENT_TAG)
                            .commit();
                }
                mDataHost.load(ChatActivity.CLAIM_DATA_TAG);
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                mVotingPanelView.setVisibility(View.VISIBLE);
                mVotingPanelView.setOnClickListener(this::onTeammateClickListener);
                mVoteButton.setOnClickListener(this::onTeammateClickListener);
                view.findViewById(R.id.voting_container).setVisibility(View.GONE);
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                mVotingPanelView.setVisibility(View.GONE);
                mList.setPadding(mList.getPaddingLeft(), 0, mList.getPaddingRight(), mList.getPaddingBottom());
                view.findViewById(R.id.voting_container).setVisibility(View.GONE);
                break;
            case TeambrellaUris.FEED_CHAT:
                mVotingPanelView.setVisibility(View.GONE);
                mList.setPadding(mList.getPaddingLeft(), 0, mList.getPaddingRight(), mList.getPaddingBottom());
                view.findViewById(R.id.voting_container).setVisibility(View.GONE);
                break;

        }
        mList.setItemAnimator(null);
    }


    @Override
    protected int getContentLayout() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        super.onDataUpdated(notification);
        if (notification.isOnNext()) {
            JsonWrapper data = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
            if (metadata != null && (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                    || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)) && metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE) > 0) {
                mList.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
            }

            if (mLastRead == null) {
                JsonWrapper discussionPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
                mLastRead = discussionPart.getLong(TeambrellaModel.ATTR_DATA_LAST_READ, -1L);
            }


            JsonWrapper basicPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            JsonWrapper votingPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            if (basicPart != null) {

                switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
                    case TeambrellaUris.CLAIMS_CHAT: {
                        Observable.fromArray(basicPart).map(json -> json.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO))
                                .map(uri -> GlideApp.with(this).load(getImageLoader().getImageUrl(uri)))
                                .subscribe(requestCreator -> requestCreator
                                        .apply(new RequestOptions().transforms(new CenterCrop()
                                                , new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp))).placeholder(R.drawable.picture_background_round_4dp))
                                        .into(mIcon), throwable -> {
                                });
                        mTitleView.setText(basicPart.getString(TeambrellaModel.ATTR_DATA_MODEL));
                        JsonWrapper teamPart = data.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM);
                        mSubtitleView.setText(Html.fromHtml(getString(R.string.claim_amount_format_string, Math.round(basicPart.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT))
                                , teamPart != null ? teamPart.getString(TeambrellaModel.ATTR_DATA_CURRENCY) : "")));

                        if (votingPart == null) {
                            mVoteTitleView.setText(R.string.team_vote);
                            setClaimVoteValue(basicPart.getFloat(TeambrellaModel.ATTR_DATA_REIMBURSEMENT, -1f));
                        }
                    }
                    break;
                    case TeambrellaUris.TEAMMATE_CHAT: {
                        Observable.fromArray(basicPart).map(json -> json.getString(TeambrellaModel.ATTR_DATA_AVATAR))
                                .map(uri -> GlideApp.with(this).load(getImageLoader().getImageUrl(uri)))
                                .subscribe(requestCreator -> requestCreator.apply(RequestOptions.circleCropTransform())
                                        .into(mIcon), throwable -> {
                                    // 8)
                                });
                        mUserName = basicPart.getString(TeambrellaModel.ATTR_DATA_NAME);
                        mTitleView.setText(mUserName);
                        mSubtitleView.setText(getString(R.string.object_format_string
                                , basicPart.getString(TeambrellaModel.ATTR_DATA_MODEL)
                                , basicPart.getString(TeambrellaModel.ATTR_DATA_YEAR)));
                        mSubtitleView.setAllCaps(true);

                        if (votingPart == null) {
                            mVoteTitleView.setText(R.string.risk);
                            setTeammateVoteValue(basicPart.getFloat(TeambrellaModel.ATTR_DATA_RISK));
                        }
                    }
                    break;
                }
            }

            if (votingPart != null) {
                switch (TeambrellaUris.sUriMatcher.match(mDataHost.getChatUri())) {
                    case TeambrellaUris.CLAIMS_CHAT:
                        setClaimVoteValue(votingPart.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, -1f));
                        break;
                    case TeambrellaUris.TEAMMATE_CHAT: {
                        setTeammateVoteValue(votingPart.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, -1f));
                    }
                    break;
                }
            } else {
                mVoteButton.setVisibility(View.GONE);
            }
        }

        if (mLastRead != null && mLastRead >= 0) {
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
            mLastRead = -1L;
        }
    }


    private void setClaimVoteValue(float value) {
        if (value >= 0) {
            mVoteValueView.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (value * 100))));
        } else {
            mVoteValueView.setText(R.string.no_teammate_vote_value);
        }
    }

    private void setTeammateVoteValue(float value) {
        if (value >= 0) {
            mVoteValueView.setText(String.format(Locale.US, "%.2f", value));
        } else {
            mVoteValueView.setText(R.string.no_teammate_vote_value);
        }
    }

    private void onTeammateClickListener(View v) {
        switch (v.getId()) {
            case R.id.voting_panel:
            case R.id.vote:
                startActivityForResult(TeammateActivity.getIntent(getContext(), mDataHost.getTeamId(), mDataHost.getUserId(), mUserName, mDataHost.getImageUri()), 10);
                break;
        }
    }

    private void onClaimClickListener(View v) {
        switch (v.getId()) {
            case R.id.voting_panel:
            case R.id.vote:
                startActivityForResult(ClaimActivity.getLaunchIntent(getContext(), mDataHost.getClaimId(), mDataHost.getObjectName(), mDataHost.getTeamId()), 10);
                break;
        }
    }


}
