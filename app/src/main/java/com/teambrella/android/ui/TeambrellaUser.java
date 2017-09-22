package com.teambrella.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.teambrella.android.BuildConfig;

/**
 * Teambrella User
 */
public class TeambrellaUser {


    private static final String PREFERENCE_NAME = "teambrella_user";
    private static final String PREFERENCE_PRIVATE_KEY = "private_key";
    private static final String PREFERENCE_USER_ID = "user_id_key";
    private static final String PREFERENCE_TEAM_ID = "team_id_key";
    private static final String PREFERENCE_DEMO_KEY = "demo_private_key";


    private static TeambrellaUser sUser;


    private SharedPreferences mPreferences;


    private TeambrellaUser(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }


    public static synchronized TeambrellaUser get(Context context) {
        if (sUser == null) {
            sUser = new TeambrellaUser(context.getApplicationContext());
        }
        return sUser;
    }

    public String getPrivateKey() {
        return mPreferences.getString(PREFERENCE_PRIVATE_KEY, BuildConfig.MASTER_USER_PRIVATE_KEY);
    }


    public boolean isDemoUser() {
        return TextUtils.isEmpty(mPreferences.getString(PREFERENCE_PRIVATE_KEY, null));
    }


    public String getUserId() {
        return mPreferences.getString(PREFERENCE_USER_ID, null);
    }

    public void setUserId(String id) {
        mPreferences.edit().putString(PREFERENCE_USER_ID, id).apply();
    }

    public void setPrivateKey(String privateKey) {
        mPreferences.edit().putString(PREFERENCE_PRIVATE_KEY, privateKey).apply();
    }

    public void setTeamId(int teamId) {
        mPreferences.edit().putInt(PREFERENCE_TEAM_ID, teamId).apply();
    }

    public int getTeamId() {
        return mPreferences.getInt(PREFERENCE_TEAM_ID, -1);
    }

}
