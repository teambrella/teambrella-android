package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;

/**
 * Teammate screen.
 */
public class TeammateActivity extends ADataHostActivity {

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

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, TeammateFragment.getInstance(DATA_FRAGMENT), UI_FRAGMENT)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getIntent().getStringExtra(TEAMMATE_NAME));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected String[] getDataTags() {
        return new String[]{DATA_FRAGMENT};
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
    protected TeambrellaDataFragment getDataFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT:
                return TeambrellaDataFragment
                        .getInstance(getIntent().getParcelableExtra(TEAMMATE_URI));
        }
        return null;
    }
}
