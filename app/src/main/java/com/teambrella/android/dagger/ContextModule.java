package com.teambrella.android.dagger;

import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Context Module
 */
@Module
public class ContextModule {

    private Context mContext;

    public ContextModule(Context context) {
        this.mContext = context;
    }

    @Provides
    @Singleton
    @Named(Dependencies.CONTEXT)
    public Context getContext() {
        return mContext;
    }
}
