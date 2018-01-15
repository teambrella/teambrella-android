package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.TeambrellaUser;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Base Teambrella Activity
 */
public abstract class ATeambrellaActivity extends TeambrellaDataHostActivity {

    @Inject
    @Named(Dependencies.TEAMBRELLA_USER)
    TeambrellaUser mUser;

    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mImageLoader;

    protected final TeambrellaUser getUser() {
        return mUser;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    protected TeambrellaImageLoader getImageLoader() {
        return mImageLoader;
    }
}
