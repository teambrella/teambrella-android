package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataProgressFragment;

import io.reactivex.Notification;

/**
 * Claim fragment
 */
public class ClaimFragment extends ADataProgressFragment<IDataHost> {

    private ImageView mClaimPicture;
    private ImageView mTeammatePicture;
    private TextView mMessageTitle;
    private TextView mMessageText;
    private TextView mUnreadCount;

    public static ClaimFragment getInstance(String tag) {
        return ADataProgressFragment.getInstance(tag, ClaimFragment.class);
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim, container, false);
        mClaimPicture = (ImageView) view.findViewById(R.id.claim_picture);
        mTeammatePicture = (ImageView) view.findViewById(R.id.teammate_picture);
        mMessageTitle = (TextView) view.findViewById(R.id.message_title);
        mMessageText = (TextView) view.findViewById(R.id.message_text);
        mUnreadCount = (TextView) view.findViewById(R.id.unread);
        if (savedInstanceState == null) {
            mDataHost.load(mTag);
            setContentShown(false);
        }
        return view;
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
                    Picasso.with(getContext())
                            .load(TeambrellaServer.AUTHORITY + photos.get(0).getAsString())
                            .into(mClaimPicture);
                }

                String avatar = claimBasic.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString();
                if (avatar != null) {
                    Picasso.with(getContext()).load(TeambrellaServer.AUTHORITY + avatar).into(mTeammatePicture);
                }
            }

            JsonObject claimDiscussion = data.get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject();
            if (claimDiscussion != null) {
                mMessageTitle.setText(getString(R.string.claim_title_format_string, data.get(TeambrellaModel.ATTR_DATA_ID).getAsInt()));
                mMessageText.setText(claimDiscussion.get(TeambrellaModel.ATTR_DATA_ORIGINAL_POST_TEXT).getAsString());
                mUnreadCount.setText(Integer.toString(claimDiscussion.get(TeambrellaModel.ATTR_DATA_UNREAD_COUNT).getAsInt()));
            }
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }
}

