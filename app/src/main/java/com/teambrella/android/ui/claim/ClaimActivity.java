package com.teambrella.android.ui.claim;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.teammate.TeammateActivity;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Claim Activity
 */
public class ClaimActivity extends ADataHostActivity implements IClaimActivity {

    private static final String DATA_TAG = "data";
    private static final String UI_TAG = "ui";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_MODEL = "model";


    private Disposable mDisposal;


    public static Intent getLaunchIntent(Context context, Uri uri, String model) {
        return new Intent(context, ClaimActivity.class).putExtra(EXTRA_URI, uri).putExtra(EXTRA_MODEL, model);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(UI_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.container, ClaimFragment.getInstance(DATA_TAG), UI_TAG).commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.title)).setText(getIntent().getStringExtra(EXTRA_MODEL));
    }


    @Override
    protected void onStart() {
        super.onStart();
        mDisposal = getObservable(DATA_TAG).subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
        }
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return TeambrellaDataFragment.getInstance(getIntent().getParcelableExtra(EXTRA_URI));
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{};
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        return null;
    }

    @Override
    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    @Override
    public void setSubtitle(String subtitle) {
        ((TextView) findViewById(R.id.subtitle)).setText(subtitle);
    }

    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            if (basic != null) {
                String avatar = basic.getString(TeambrellaModel.ATTR_DATA_AVATAR);
                if (avatar != null) {
                    ImageView teammatePicture = (ImageView) findViewById(R.id.teammate_picture);
                    String pictureUri = TeambrellaServer.AUTHORITY + avatar;
                    TeambrellaImageLoader.getInstance(this).getPicasso()
                            .load(pictureUri).into(teammatePicture);
                    teammatePicture.setOnClickListener(v ->
                            startActivity(TeammateActivity.getIntent(ClaimActivity.this
                                    , TeambrellaUris.getTeammateUri(BuildConfig.TEAM_ID
                                            , basic.getString(TeambrellaModel.ATTR_DATA_USER_ID))
                                    , basic.getString(TeambrellaModel.ATTR_DATA_NAME)
                                    , pictureUri)));
                }
            }
        }
    }
}
