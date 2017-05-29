package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.data.base.TeambrellaDataFragment;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teammate screen.
 */
public class TeammateActivity extends AppCompatActivity implements ITeammateDataHost {

    private static final String TEAMMATE_URI = "teammate_uri";
    private static final String TEAMMATE_NAME = "teammate_name";
    private static final String TEAMMATE_PICTURE = "teammate_picture";

    private static final String DATA_FRAGMENT = "data";
    private static final String UI_FRAGMENT = "ui";

    /**
     * Get intent to launch activity
     *
     * @param context to use
     * @param uri     teammate uri
     * @return intent to start activity
     */
    public static Intent getIntent(Context context, Uri uri, String name, String userPictureUri) {
        return new Intent(context, TeammateActivity.class)
                .putExtra(TEAMMATE_URI, uri)
                .putExtra(TEAMMATE_NAME, name)
                .putExtra(TEAMMATE_URI, uri);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_teammate);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(DATA_FRAGMENT) == null) {
            fragmentManager.beginTransaction()
                    .add(TeambrellaDataFragment
                            .getInstance(getIntent().getParcelableExtra(TEAMMATE_URI)), DATA_FRAGMENT)
                    .commit();
        }

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, new TeammateFragment(), UI_FRAGMENT)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getIntent().getStringExtra(TEAMMATE_NAME));
    }

    @Override
    public Observable<Notification<JsonObject>> getTeammateObservable() {
        TeambrellaDataFragment fragment = (TeambrellaDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAGMENT);
        return fragment != null ? fragment.getObservable() : null;
    }

    @Override
    public void loadTeammate() {
        TeambrellaDataFragment fragment = (TeambrellaDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAGMENT);
        if (fragment != null) {
            fragment.load();
        }
    }
}
