package com.teambrella.android.ui.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.util.log.Log;

/**
 * App is outdated.
 */
public class AppOutdatedActivity extends AppCompatActivity {

    private static final String LOG_TAG = AppOutdatedActivity.class.getSimpleName();
    private static final String EXTRA_CRITICAL = "extra_critical";

    public static void start(Context context, boolean critical) {
        context.startActivity(new Intent(context, AppOutdatedActivity.class).putExtra(EXTRA_CRITICAL, critical));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdated);
        final TextView description = findViewById(R.id.description);
        final TextView action = findViewById(R.id.action);
        final TextView back = findViewById(R.id.back);
        final Intent intent = getIntent();
        boolean critical = intent != null && intent.getBooleanExtra(EXTRA_CRITICAL, false);
        description.setText(critical ? R.string.app_is_outdated_description_critical : R.string.app_is_outdated_description);
        action.setOnClickListener(v -> {
            try {
                startActivity(new Intent(android.content.Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            } catch (Exception e) {
                Log.reportNonFatal(LOG_TAG, e);
            }
            finish();
        });

        if (critical) {
            back.setVisibility(View.INVISIBLE);
        } else {
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(v -> finish());
        }

    }
}
