package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.squareup.picasso.Picasso;
import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.ui.TeambrellaUser;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Base Teambrella Activity
 */
public abstract class ATeambrellaActivity extends TeambrellaDataHostActivity {

    @Inject
    @Named(Dependencies.PICASSO)
    Picasso mPicasso;

    @Inject
    @Named(Dependencies.TEAMBRELLA_USER)
    TeambrellaUser mUser;

    protected final Picasso getPicasso() {
        return mPicasso;
    }

    protected final TeambrellaUser getUser() {
        return mUser;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPicasso != null) {
            mPicasso.shutdown();
        }
    }
}
