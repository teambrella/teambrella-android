package com.teambrella.android.dagger;

import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Image Loader Module
 */
@Module
public class TeambrellaImageLoaderModule {
    @Provides
    @Singleton
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader getImageLoader(@Named(Dependencies.TEAMBRELLA_SERVER) TeambrellaServer server) {
        return new TeambrellaImageLoader(server);
    }
}
