package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.squareup.picasso.Picasso;
import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.dagger.PicassoModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;
import com.teambrella.android.ui.base.dagger.ADaggerActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Teambrella Dagger Activity
 */
public abstract class TeambrellaDaggerActivity extends ADaggerActivity<ITeambrellaComponent> implements ITeambrellaDaggerActivity {

    @Inject
    @Named(Dependencies.PICASSO)
    Picasso mPicasso;


    @Override
    protected ITeambrellaComponent createComponent() {
        return DaggerITeambrellaComponent.builder()
                .contextModule(new ContextModule(this))
                .teambrellaUserModule(new TeambrellaUserModule())
                .teambrellaServerModule(new TeambrellaServerModule())
                .picassoModule(new PicassoModule())
                .build();
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
