package com.teambrella.android.ui.claim;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.teambrella.android.R;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.ui.base.ADataHostActivity;

/**
 * Claim Activity
 */
public class ClaimActivity extends ADataHostActivity {

    private static final String DATA_TAG = "data";
    private static final String UI_TAG = "ui";
    private static final String EXTRA_URI = "uri";


    public static Intent getLaunchIntent(Context context, Uri uri) {
        return new Intent(context, ClaimActivity.class).putExtra(EXTRA_URI, uri);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(UI_TAG) == null) {
            fragmentManager.beginTransaction().add(R.id.container, ClaimFragment.getInstance(DATA_TAG), UI_TAG).commit();
        }
    }

    @Override
    protected String[] getDataTag() {
        return new String[]{DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return TeambrellaDataFragment.getInstance(getIntent().getParcelableExtra(EXTRA_URI));
    }
}
