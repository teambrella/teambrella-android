package com.teambrella.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.multidex.MultiDexApplication;

/**
 * Teambrella Application
 */
public class TeambrellaApplication extends MultiDexApplication {

    public static final String ACCOUNT = "com.teambrella.android";
    public static final String ACCOUNT_TYPE = "com.teambrella.android.teammate";

    @Override
    public void onCreate() {
        super.onCreate();
        createSyncAccount();
    }


    /**
     * Create sync account
     */
    private void createSyncAccount() {
        // Create the account type and default account
        Account account = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);

        Account accounts[] = accountManager.getAccountsByTypeForPackage(ACCOUNT_TYPE, getPackageName());

        if (accounts.length == 0) {
            accountManager.addAccountExplicitly(account, BuildConfig.PRIVATE_KEY, null);
        }


    }
}
