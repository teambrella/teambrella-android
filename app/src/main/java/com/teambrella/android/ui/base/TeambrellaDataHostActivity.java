package com.teambrella.android.ui.base;

import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.PicassoModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;

/**
 * Teambrella Data Host Activity.
 */
public abstract class TeambrellaDataHostActivity extends ADataHostActivity<ITeambrellaComponent> {

    @Override
    protected ITeambrellaComponent createComponent() {
        return DaggerITeambrellaComponent.builder()
                .contextModule(new ContextModule(this))
                .teambrellaUserModule(new TeambrellaUserModule())
                .teambrellaServerModule(new TeambrellaServerModule())
                .picassoModule(new PicassoModule())
                .build();
    }
}
