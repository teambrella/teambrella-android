package com.teambrella.android.ui.base;

import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.TeambrellaImageLoaderModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;

/**
 * Teambrella Data Host Activity.
 */
public abstract class TeambrellaDataHostActivity extends ADataHostActivity<ITeambrellaComponent> implements ITeambrellaDaggerActivity {

    @Override
    protected ITeambrellaComponent createComponent() {
        return DaggerITeambrellaComponent.builder()
                .contextModule(new ContextModule(this))
                .teambrellaUserModule(new TeambrellaUserModule())
                .teambrellaServerModule(new TeambrellaServerModule())
                .teambrellaImageLoaderModule(new TeambrellaImageLoaderModule())
                .build();
    }
}
