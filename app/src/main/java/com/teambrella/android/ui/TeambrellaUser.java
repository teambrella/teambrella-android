package com.teambrella.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

/**
 * Teambrella User
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaUser {


    private static final String PREFERENCE_NAME = "teambrella_user";
    private static final String PREFERENCE_PRIVATE_KEY = "private_key";
    private static final String PREFERENCE_USER_ID = "user_id_key";
    private static final String PREFERENCE_TEAM_ID = "team_id_key";
    private static final String PREFERENCE_DEMO_KEY = "demo_private_key";
    private static final String PREFERENCE_PENDING_KEY = "pending_private_key";


    private static TeambrellaUser sUser;


    private SharedPreferences mPreferences;


    public TeambrellaUser(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        if (TextUtils.isEmpty(mPreferences.getString(PREFERENCE_PENDING_KEY, null))) {
            mPreferences.edit().putString(PREFERENCE_PENDING_KEY, generatePrivateKey()).apply();
        }
        if (TextUtils.isEmpty(mPreferences.getString(PREFERENCE_DEMO_KEY, null))) {
            mPreferences.edit().putString(PREFERENCE_DEMO_KEY, generatePrivateKey()).apply();
        }
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


    public void setDemoUser() {
        setPrivateKey(mPreferences.getString(PREFERENCE_DEMO_KEY, null));
    }

    public void resetDemoUser() {
        mPreferences.edit().remove(PREFERENCE_PRIVATE_KEY).apply();
        mPreferences.edit().putString(PREFERENCE_DEMO_KEY, generatePrivateKey()).apply();
        setUserId(null);
    }

    public boolean isDemoUser() {
        return mPreferences.getString(PREFERENCE_DEMO_KEY, "")
                .equals(mPreferences.getString(PREFERENCE_PRIVATE_KEY, null));
    }

    public String getPendingPrivateKey() {
        return mPreferences.getString(PREFERENCE_PENDING_KEY, null);
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


    private String generatePrivateKey() {
        return new Wallet(MainNetParams.get())
                .getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION).getPrivateKeyAsWiF(MainNetParams.get());
    }

}
