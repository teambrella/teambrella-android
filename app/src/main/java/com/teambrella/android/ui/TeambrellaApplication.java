package com.teambrella.android.ui;

import android.support.multidex.MultiDexApplication;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Teambrella Application
 */
public class TeambrellaApplication extends MultiDexApplication {

    public static final String ACCOUNT = "com.teambrella.android";
    public static final String ACCOUNT_TYPE = "com.teambrella.android.teammate";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
//        createSyncAccount();
    }


//    /**
//     * Create sync account
//     */
//    private void createSyncAccount() {
//        // Create the account type and default account
//        Account account = new Account(
//                ACCOUNT, ACCOUNT_TYPE);
//        // Get an instance of the Android account manager
//        AccountManager accountManager =
//                (AccountManager) getSystemService(
//                        ACCOUNT_SERVICE);
//
//        Account accounts[] = accountManager.getAccountsByTypeForPackage(ACCOUNT_TYPE, getPackageName());
//
//        if (accounts.length == 0) {
//            accountManager.addAccountExplicitly(account, BuildConfig.PRIVATE_KEY, null);
//        }
//
//
//    }
}
