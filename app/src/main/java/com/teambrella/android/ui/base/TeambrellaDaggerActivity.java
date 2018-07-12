package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.dagger.TeambrellaImageLoaderModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Teambrella Dagger Activity
 */
public abstract class TeambrellaDaggerActivity extends ATeambrellaDaggerActivity<ITeambrellaComponent> implements ITeambrellaDaggerActivity {

    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mImageLoader;


    @Override
    protected ITeambrellaComponent createComponent() {
        return DaggerITeambrellaComponent.builder()
                .contextModule(new ContextModule(this))
                .teambrellaUserModule(new TeambrellaUserModule())
                .teambrellaServerModule(new TeambrellaServerModule())
                .teambrellaImageLoaderModule(new TeambrellaImageLoaderModule())
                .build();
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
