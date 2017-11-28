package com.teambrella.android.ui.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teambrella.android.R;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.WelcomeActivity;
import com.teambrella.android.util.StatisticHelper;

/**
 * New Demo Session Error Screen.
 */
public class NewDemoSessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_demo_session);
        findViewById(R.id.try_demo).setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
        findViewById(R.id.back).setOnClickListener(v -> {
            TeambrellaUser.get(this).resetDemoUser();
            StatisticHelper.setUserId(null);
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }
}
