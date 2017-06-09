package com.teambrella.android.ui.claim;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.widget.ImagePager;

import java.util.ArrayList;

import io.reactivex.Notification;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Claim fragment
 */
public class ClaimFragment extends ADataProgressFragment<IClaimActivity> {

    private static final String DETAILS_FRAGMENT_TAG = "details";

    private ImagePager mClaimPictures;
    private ImageView mOriginalObjectPicture;
    private TextView mMessageTitle;
    private TextView mMessageText;
    private TextView mUnreadCount;

    public static ClaimFragment getInstance(String tag) {
        return ADataProgressFragment.getInstance(tag, ClaimFragment.class);
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim, container, false);
        mClaimPictures = (ImagePager) view.findViewById(R.id.image_pager);
        mOriginalObjectPicture = (ImageView) view.findViewById(R.id.object_picture);
        mMessageTitle = (TextView) view.findViewById(R.id.message_title);
        mMessageText = (TextView) view.findViewById(R.id.message_text);
        mUnreadCount = (TextView) view.findViewById(R.id.unread);

        view.findViewById(R.id.swipe_to_refresh).setEnabled(false);

        if (savedInstanceState == null) {
            mDataHost.load(mTag);
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
        if (fragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.details_container, ClaimDetailsFragment.getInstance(mTag), DETAILS_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject response = notification.getValue();
            JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            JsonObject claimBasic = data.get(TeambrellaModel.ATTR_DATA_ONE_BASIC).getAsJsonObject();
            if (claimBasic != null) {
                JsonArray photos = claimBasic.get(TeambrellaModel.ATTR_DATA_SMALL_PHOTOS).getAsJsonArray();
                if (photos != null && photos.size() > 0) {
                    ArrayList<String> list = new ArrayList<>(photos.size());
                    for (int i = 0; i < photos.size(); i++) {
                        list.add(TeambrellaServer.AUTHORITY + photos.get(i).getAsString());
                    }
                    mClaimPictures.init(getChildFragmentManager(), list);
                }

                getActivity().setTitle(claimBasic.get(TeambrellaModel.ATTR_DATA_MODEL).getAsString());
            }

            JsonObject claimDiscussion = data.get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject();
            if (claimDiscussion != null) {
                mMessageTitle.setText(getString(R.string.claim_title_format_string, data.get(TeambrellaModel.ATTR_DATA_ID).getAsInt()));
                mMessageText.setText(claimDiscussion.get(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT).getAsString());
                mUnreadCount.setText(Integer.toString(claimDiscussion.get(TeambrellaModel.ATTR_DATA_UNREAD_COUNT).getAsInt()));

                String objectPhoto = claimDiscussion.get(TeambrellaModel.ATTR_DATA_SMALL_PHOTO).getAsString();
                if (objectPhoto != null) {
                    Resources resources = getResources();
                    TeambrellaImageLoader.getInstance(getContext()).getPicasso()
                            .load(TeambrellaServer.AUTHORITY + objectPhoto).resize(resources.getDimensionPixelSize(R.dimen.claim_object_picture_with), resources.getDimensionPixelSize(R.dimen.claim_object_picture_height)).centerCrop()
                            .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask)).into(mOriginalObjectPicture);
                }
            }
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }
}

