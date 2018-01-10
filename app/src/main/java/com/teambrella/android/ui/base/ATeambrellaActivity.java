package com.teambrella.android.ui.base;

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
}
