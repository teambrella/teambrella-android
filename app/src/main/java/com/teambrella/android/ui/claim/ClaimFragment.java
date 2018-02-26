package com.teambrella.android.ui.claim;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.widget.ImagePager;
import com.teambrella.android.util.ConnectivityUtils;

import java.util.ArrayList;

import io.reactivex.Notification;

/**
 * Claim fragment
 */
public class ClaimFragment extends ADataProgressFragment<IClaimActivity> {

    private static final String DETAILS_FRAGMENT_TAG = "details";
    private static final String VOTING_FRAGMENT_TAG = "voting";

    private ImagePager mClaimPictures;
    private ImageView mOriginalObjectPicture;
    private TextView mMessageTitle;
    private TextView mMessageText;
    private TextView mUnreadCount;
    private TextView mWhen;
    private View mDiscussion;
    private View mDiscussionForeground;

    private boolean mIsShown;

    private int mTeamAccessLevel = TeambrellaModel.TeamAccessLevel.FULL_ACCESS;

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim, container, false);
        mClaimPictures = view.findViewById(R.id.image_pager);
        mOriginalObjectPicture = view.findViewById(R.id.object_picture);
        mMessageTitle = view.findViewById(R.id.message_title);
        mMessageText = view.findViewById(R.id.message_text);
        mUnreadCount = view.findViewById(R.id.unread);
        mDiscussion = view.findViewById(R.id.discussion);
        mWhen = view.findViewById(R.id.when);
        mDiscussionForeground = view.findViewById(R.id.discussion_foreground);
        view.findViewById(R.id.swipe_to_refresh).setEnabled(false);
        mDataHost.load(mTags[0]);
        setContentShown(false);
        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) == null) {
            transaction.add(R.id.details_container, ClaimDetailsFragment.getInstance(mTags), DETAILS_FRAGMENT_TAG);
        }

        if (fragmentManager.findFragmentByTag(VOTING_FRAGMENT_TAG) == null) {
            transaction.add(R.id.voting_container, KClaimVotingResultFragmentKt.getInstance(mTags, KClaimVotingResultFragmentKt.MODE_CLAIM), VOTING_FRAGMENT_TAG);
        }


        if (!transaction.isEmpty()) {
            transaction.commit();
        }
        mIsShown = false;

    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper claimBasic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            JsonWrapper team = data.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM);

            final String smallPhoto;

            if (claimBasic != null) {
                ArrayList<String> photos = TeambrellaModel.getImages("",
                        claimBasic.getObject(), TeambrellaModel.ATTR_DATA_BIG_PHOTOS);
                if (photos != null && photos.size() > 0) {
                    mClaimPictures.init(getChildFragmentManager(), photos);
                    smallPhoto = photos.get(0);
                } else {
                    smallPhoto = null;
                }
                getActivity().setTitle(claimBasic.getString(TeambrellaModel.ATTR_DATA_MODEL));
            } else {
                smallPhoto = null;
            }

            JsonWrapper claimDiscussion = data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
            if (claimDiscussion != null) {
                mMessageTitle.setText(getString(R.string.claim_title_format_string, data.getInt(TeambrellaModel.ATTR_DATA_ID, 0)));
                String text = claimDiscussion.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT);

                if (text != null) {
                    mMessageText.setText(text.replaceAll("<p>", "").replaceAll("</p>", ""));
                }


                int unread = claimDiscussion.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT, 0);
                mUnreadCount.setText(Integer.toString(unread));
                mUnreadCount.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);

                String objectPhoto = claimDiscussion.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
                if (objectPhoto != null) {
                    GlideApp.with(this).load(getImageLoader().getImageUrl(objectPhoto))
                            .apply(new RequestOptions().transforms(new CenterCrop()
                                    , new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp))))
                            .into(mOriginalObjectPicture);
                }

                long now = System.currentTimeMillis();
                long when = now - 60000 * claimDiscussion.getInt(TeambrellaModel.ATTR_DATA_SINCE_LAST_POST_MINUTES);
                mWhen.setText(DateUtils.getRelativeTimeSpanString(when, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));


                final int claimId = data.getInt(TeambrellaModel.ATTR_DATA_ID);
                final String topicId = claimDiscussion.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID);
                final Uri uri = claimId > 0 ? TeambrellaUris.getClaimChatUri(claimId) : null;

                mTeamAccessLevel = team != null ? team.getInt(TeambrellaModel.ATTR_DATA_TEAM_ACCESS_LEVEL, mTeamAccessLevel) : mTeamAccessLevel;

                if (uri != null) {
                    mDiscussionForeground.setOnClickListener(v -> mDataHost.launchActivity(ChatActivity.getClaimChat(getContext()
                            , mDataHost.getTeamId()
                            , claimId
                            , claimBasic != null ? claimBasic.getString(TeambrellaModel.ATTR_DATA_MODEL) : null
                            , smallPhoto
                            , topicId
                            , mTeamAccessLevel)));
                }
            }

            JsonWrapper claimVoting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            if (claimVoting != null) {
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.voting_container).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.details_container).setBackgroundResource(R.drawable.block_last);
                }
            }
            setContentShown(true);
            mIsShown = true;
        } else {
            setContentShown(true, !mIsShown);
            mDataHost.showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext()) ? R.string.something_went_wrong_error : R.string.no_internet_connection);
        }
    }
}

