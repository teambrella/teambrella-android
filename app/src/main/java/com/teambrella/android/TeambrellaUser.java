package com.teambrella.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Teambrella User
 */
public class TeambrellaUser {


    private static final String PREFERENCE_NAME = "teambrella_user";
    private static final String PREFERENCE_PRIVATE_KEY = "private_key";


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

    public void setPrivateKey(String privateKey) {
        mPreferences.edit().putString(PREFERENCE_PRIVATE_KEY, privateKey).apply();
    }
}
