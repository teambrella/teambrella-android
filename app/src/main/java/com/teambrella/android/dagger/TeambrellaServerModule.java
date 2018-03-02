package com.teambrella.android.dagger;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Teambrella Server Module.
 */
@Module
public class TeambrellaServerModule {
    @Provides
    @Singleton
    @Named(Dependencies.TEAMBRELLA_SERVER)
    TeambrellaServer getTeambrellaServer(@Named(Dependencies.CONTEXT) Context context, @Named(Dependencies.TEAMBRELLA_USER) TeambrellaUser user) {
        return new TeambrellaServer(context, user.getPrivateKey(), user.getDeviceCode(), !user.isDemoUser() ? FirebaseInstanceId.getInstance().getToken() : null
                , user.getInfoMask());
    }
}
