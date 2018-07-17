package com.teambrella.android.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.chat.inbox.InboxActivity;

import io.reactivex.Notification;

/**
 * Main Landing Fragment
 */
public class AMainLandingFragment extends ADataFragment<IMainDataHost> {


    private TextView mTitle;
    private TextView mUnreadCount;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView teamLogo = view.findViewById(R.id.team_logo);


        GlideApp.with(this).load(getImageLoader().getImageUrl(getDataHost().getTeamLogoUri()))
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp))))
                .into(teamLogo);


        teamLogo.setOnClickListener(v -> getDataHost().showTeamChooser());
        view.findViewById(R.id.arrow_down).setOnClickListener(v -> getDataHost().showTeamChooser());

        mTitle = view.findViewById(R.id.title);
        mUnreadCount = view.findViewById(R.id.unread_count);
        view.findViewById(R.id.inbox).setOnClickListener(v -> getDataHost().launchActivity(new Intent(getContext(), InboxActivity.class)));
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            if (data != null) {
                int unreadCount = data.getInt(TeambrellaModel.ATTR_DATA_UNREAD_COUNT);
                mUnreadCount.setVisibility(unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
                mUnreadCount.setText(Integer.toString(unreadCount));
            }
        }
    }

    protected void setTitle(String title) {
        mTitle.setText(title);
    }
}
