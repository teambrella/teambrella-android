package com.teambrella.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.chat.inbox.InboxActivity;

import io.reactivex.Notification;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Main Landing Fragment
 */
public class AMainLandingFragment extends ADataFragment<IMainDataHost> {


    private TextView mTitle;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView teamLogo = view.findViewById(R.id.team_logo);


        TeambrellaImageLoader.getInstance(getContext()).getPicasso().load(mDataHost.getTeamLogoUri())
                .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask))
                .into(teamLogo);

        teamLogo.setOnClickListener(v -> mDataHost.showTeamChooser());


        mTitle = view.findViewById(R.id.title);

        view.findViewById(R.id.inbox).setOnClickListener(v -> startActivity(new Intent(getContext(), InboxActivity.class)));
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {

    }

    protected void setTitle(String title) {
        mTitle.setText(title);
    }
}
