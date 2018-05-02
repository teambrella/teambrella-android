package com.teambrella.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.provider.FontRequest;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.leakcanary.LeakCanary;
import com.teambrella.android.services.TeambrellaNotificationManager;
import com.teambrella.android.ui.TeambrellaUser;

import java.util.HashSet;

import io.fabric.sdk.android.Fabric;

/**
 * Teambrella Application
 */
public class TeambrellaApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;


    private HashSet<Activity> mActivities = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);

        //noinspection ConstantConditions
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        //noinspection ResultOfMethodCallIgnored
        LeakCanary.install(this);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics(), new Answers());
            Crashlytics.setUserIdentifier(TeambrellaUser.get(this).getUserId());
        }

        sAnalytics = GoogleAnalytics.getInstance(this);
        sAnalytics.setDryRun(BuildConfig.DEBUG);

        registerActivityLifecycleCallbacks(this);

        TeambrellaNotificationManager.recreateNotificationChannels(this);
        
        final FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        FontRequestEmojiCompatConfig config = new FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest);
        EmojiCompat.init(config);

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

    synchronized public FirebaseAnalytics getFireBaseAnalytics() {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        analytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        analytics.setUserId(TeambrellaUser.get(this).getUserId());
        return analytics;
    }

    public boolean isForeground() {
        return mActivities.size() > 0;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mActivities.add(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mActivities.remove(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
