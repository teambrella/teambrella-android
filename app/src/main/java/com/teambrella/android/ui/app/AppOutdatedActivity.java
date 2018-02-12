package com.teambrella.android.ui.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teambrella.android.R;
import com.teambrella.android.ui.TeambrellaUser;

/**
 * App is outdated.
 */
public class AppOutdatedActivity extends AppCompatActivity {

    private static final String EXTRA_CRITICAL = "extra_critical";
    //private static final long MIN_DELAY = 1000 * 60 * 60* 24 * 3;
    private static final long MIN_DELAY = 1000 * 60;

    public static void start(Context context, boolean critical) {
        TeambrellaUser user = TeambrellaUser.get(context);
        long current = System.currentTimeMillis();
        if (!critical && Math.abs(current - user.getNewVersionLastNotificationTime()) < MIN_DELAY) {
            return;
        }
        context.startActivity(new Intent(context, AppOutdatedActivity.class).putExtra(EXTRA_CRITICAL, critical)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        user.setNewVersionLastNotificationTime(current);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdated);
    }
}
