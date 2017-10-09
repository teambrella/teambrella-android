package com.teambrella.android.ui.base;

import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.PicassoModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;
import com.teambrella.android.data.base.TeambrellaDataLoader;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.chat.ChatDataPagerLoader;

import javax.inject.Singleton;

import dagger.Component;

/**
 * ITeambrella Component
 */
@Singleton
@Component(modules = {ContextModule.class, TeambrellaUserModule.class, TeambrellaServerModule.class, PicassoModule.class})
public interface ITeambrellaComponent {
    void inject(MainActivity activity);

    void inject(TeambrellaDataLoader loader);

    void inject(TeambrellaDataPagerLoader loader);

    void inject(ChatDataPagerLoader loader);
}
