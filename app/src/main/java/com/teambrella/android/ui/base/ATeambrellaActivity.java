package com.teambrella.android.ui.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.util.log.Log;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Base Teambrella Activity
 */
public abstract class ATeambrellaActivity extends TeambrellaDataHostActivity {

    public static final String EXTRA_BACK_PRESSED_INTENT = "extra_back_pressed_intent";

    private static final String LOG_TAG = ATeambrellaActivity.class.getSimpleName();

    @Named(Dependencies.TEAMBRELLA_USER)
    TeambrellaUser mUser;

    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mImageLoader;

    protected final TeambrellaUser getUser() {
        return mUser;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    protected TeambrellaImageLoader getImageLoader() {
        return mImageLoader;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final Intent intent = getIntent();
        final PendingIntent backPressedIntent = intent != null ? intent.getParcelableExtra(EXTRA_BACK_PRESSED_INTENT) : null;
        if (backPressedIntent != null) {
            try {
                backPressedIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
}
