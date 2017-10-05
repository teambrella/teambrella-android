package com.teambrella.android;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.teambrella.android.ui.TeambrellaUser;

import io.fabric.sdk.android.Fabric;

/**
 * Teambrella Application
 */
public class TeambrellaApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics(), new Answers());
            Crashlytics.setUserIdentifier(TeambrellaUser.get(this).getUserId());
        }
    }
}
