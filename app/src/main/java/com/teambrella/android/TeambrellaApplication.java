package com.teambrella.android;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.teambrella.android.ui.TeambrellaUser;

import io.fabric.sdk.android.Fabric;

/**
 * Teambrella Application
 */
public class TeambrellaApplication extends MultiDexApplication {

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();


        Glide.init(this, new GlideBuilder().setLogLevel(Log.VERBOSE));

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        LeakCanary.install(this);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics(), new Answers());
            Crashlytics.setUserIdentifier(TeambrellaUser.get(this).getUserId());
        }

        sAnalytics = GoogleAnalytics.getInstance(this);
        sAnalytics.setDryRun(BuildConfig.DEBUG);


    }

    /**
     * Gets the default {@link Tracker} for this {@link android.app.Application}.
     *
     * @return tracker
     */
    synchronized public Tracker geTracker() {
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.ga_tracker);
            sTracker.enableAutoActivityTracking(true);
        }
        sTracker.set("&uid", TeambrellaUser.get(this).getUserId());
        return sTracker;
    }
}
