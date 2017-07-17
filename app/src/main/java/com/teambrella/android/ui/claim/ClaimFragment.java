package com.teambrella.android.ui.claim;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.chat.ChatActivity;
import com.teambrella.android.ui.widget.ImagePager;

import java.util.ArrayList;

import io.reactivex.Notification;
import jp.wasabeef.picasso.transformations.MaskTransformation;

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
    private View mDiscussion;

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim, container, false);
        mClaimPictures = (ImagePager) view.findViewById(R.id.image_pager);
        mOriginalObjectPicture = (ImageView) view.findViewById(R.id.object_picture);
        mMessageTitle = (TextView) view.findViewById(R.id.message_title);
        mMessageText = (TextView) view.findViewById(R.id.message_text);
        mUnreadCount = (TextView) view.findViewById(R.id.unread);
        mDiscussion = view.findViewById(R.id.discussion);


        view.findViewById(R.id.swipe_to_refresh).setEnabled(false);

        if (savedInstanceState == null) {
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }
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
            transaction.add(R.id.voting_container, ADataFragment.getInstance(mTags, ClaimVotingFragment.class), VOTING_FRAGMENT_TAG);
        }


        if (!transaction.isEmpty()) {
            transaction.commit();
        }


    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper claimBasic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);

            if (claimBasic != null) {
                ArrayList<String> photos = TeambrellaModel.getImages(TeambrellaServer.AUTHORITY,
                        claimBasic.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTOS);
                if (photos != null && photos.size() > 0) {
                    mClaimPictures.init(getChildFragmentManager(), photos);
                }
                getActivity().setTitle(claimBasic.getString(TeambrellaModel.ATTR_DATA_MODEL));
            }

            JsonWrapper claimDiscussion = data.getObject(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
            if (claimDiscussion != null) {
                mMessageTitle.setText(getString(R.string.claim_title_format_string, data.getInt(TeambrellaModel.ATTR_DATA_ID, 0)));
                String text = claimDiscussion.getString(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT);

                if (text != null) {
                    mMessageText.setText(text);
                }

                mUnreadCount.setText(Integer.toString(claimDiscussion.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT, 0)));

                String objectPhoto = claimDiscussion.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
                if (objectPhoto != null) {
                    Resources resources = getResources();
                    TeambrellaImageLoader.getInstance(getContext()).getPicasso()
                            .load(TeambrellaServer.AUTHORITY + objectPhoto).resize(resources.getDimensionPixelSize(R.dimen.claim_object_picture_with), resources.getDimensionPixelSize(R.dimen.claim_object_picture_height)).centerCrop()
                            .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask)).into(mOriginalObjectPicture);
                }


                final int claimId = data.getInt(TeambrellaModel.ATTR_DATA_ID);
                final String topicId = data.getString(TeambrellaModel.ATTR_DATA_TOPIC_ID);
                final Uri uri = claimId > 0 ? TeambrellaUris.getClaimChatUri(claimId) : null;


                if (uri != null) {
                    mDiscussion.setOnClickListener(v -> startActivity(ChatActivity.getLaunchIntent(getContext(), uri, topicId)));
                }
            }

            JsonWrapper claimVoting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            if (claimVoting != null) {
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.voting_container).setVisibility(View.VISIBLE);
                }
            }
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }
}

