package com.teambrella.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.teambrella.android.ui.TeamFragment;

public class TeamActivity extends AppCompatActivity {

    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment contentFragment = fragmentManager.findFragmentById(R.id.activity_content);
        if (contentFragment == null) {
            fragmentManager.beginTransaction().replace(R.id.activity_content, new TeamFragment(), UI_FRAGMENT_TAG).commit();
        }
    }
}
