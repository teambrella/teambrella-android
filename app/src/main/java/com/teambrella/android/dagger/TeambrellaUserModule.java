package com.teambrella.android.dagger;


import android.content.Context;

import com.teambrella.android.ui.TeambrellaUser;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TeambrellaUserModule {

    @Singleton
    @Provides
    @Named(Dependencies.TEAMBRELLA_USER)
    TeambrellaUser getTeambrellaUser(@Named(Dependencies.CONTEXT) Context context) {
        return new TeambrellaUser(context.getApplicationContext());
    }
}
