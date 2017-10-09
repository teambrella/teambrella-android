package com.teambrella.android.dagger;


import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.server.TeambrellaServer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PicassoModule {

    @Provides
    @Singleton
    @Named(Dependencies.PICASSO)
    Picasso getPicasso(@Named(Dependencies.CONTEXT) Context context, @Named(Dependencies.TEAMBRELLA_SERVER) TeambrellaServer server) {
        return new Picasso.Builder(context).downloader(new OkHttp3Downloader(server.getHttpClient(context))).loggingEnabled(BuildConfig.DEBUG)
                .listener((picasso, uri, exception) -> {
                }).build();
    }
}
