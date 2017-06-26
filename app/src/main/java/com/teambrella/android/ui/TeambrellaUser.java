package com.teambrella.android.ui;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Teambrella User
 */
public class TeambrellaUser {


    private static final String PREFERENCE_NAME = "teambrella_user";
    private static final String PREFERENCE_PRIVATE_KEY = "private_key";
    private static final String PREFRENCE_TEAM_ID = "team_id";


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
        return mPreferences.getString(PREFERENCE_PRIVATE_KEY, null);
    }

    void setPrivateKey(String privateKey) {
        mPreferences.edit().putString(PREFERENCE_PRIVATE_KEY, privateKey).apply();
    }

    void setTeamId(int teamId) {
        mPreferences.edit().putInt(PREFRENCE_TEAM_ID, teamId).apply();
    }

    int getTeamId() {
        return mPreferences.getInt(PREFRENCE_TEAM_ID, -1);
    }
}
