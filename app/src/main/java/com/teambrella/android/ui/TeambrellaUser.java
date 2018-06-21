package com.teambrella.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

import java.util.UUID;

/**
 * Teambrella User
 */
@SuppressWarnings("WeakerAccess")
public class TeambrellaUser {


    private static final String PREFERENCE_NAME = "teambrella_user";
    private static final String PREFERENCE_PRIVATE_KEY = "private_key";
    private static final String PREFERENCE_USER_ID = "user_id_key";
    private static final String PREFERENCE_TEAM_ID = "team_id_key";
    private static final String PREFERENCE_DEVICE_CODE = "device_code";
    private static final String PREFERENCE_DEMO_KEY = "demo_private_key";
    private static final String PREFERENCE_PENDING_KEY = "pending_private_key";
    private static final String PREFERENCE_NOTIFICATION_TIMESTAMP = "notification_timestamp_key";
    private static final String PREFERENCE_WALLET_BACKUP_SHOWN = "wallet_backup_shown";
    private static final String PREFERENCE_KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String PREFERENCE_KEY_IS_WALLET_BACKED_UP = "is_wallet_backed_up";
    private static final String PREFERENCE_KEY_NEW_VERSION_LAST_SCREEN_TIME = "new_version_last_screen_time";
    private static final String PREFERENCE_KEY_NEW_VERSION_LAST_NOTIFICATION_TIME = "new_version_last_notification_time";
    private static final String PREFERENCE_KEY_SLIDE_TO_VOTE_SHOWN = "slide_to_vote_shown";
    private static final String PREFERENCE_KEY_BACKGROUND_APP_ACTIVITY = "background_app_activity_key";
    private static final String PREFERENCE_KEY_IS_NOTIFIED_BACKGROUND_APP_ACTIVITY = "is_notified_background_app_activity_key";
    private static final String PREFERENCE_KEY_LAST_BACK_UP_NOTIFICATION_SHOWN = "last_backup_notification_shown";

    private static TeambrellaUser sUser;


    private SharedPreferences mPreferences;


    private TeambrellaUser(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        if (TextUtils.isEmpty(mPreferences.getString(PREFERENCE_PENDING_KEY, null))) {
            mPreferences.edit().putString(PREFERENCE_PENDING_KEY, generatePrivateKey()).apply();
        }
        if (TextUtils.isEmpty(mPreferences.getString(PREFERENCE_DEMO_KEY, null))) {
            mPreferences.edit().putString(PREFERENCE_DEMO_KEY, generatePrivateKey()).apply();
        }
        if (TextUtils.isEmpty(mPreferences.getString(PREFERENCE_DEVICE_CODE, null))) {
            mPreferences.edit().putString(PREFERENCE_DEVICE_CODE, UUID.randomUUID().toString()).apply();
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


    public String getDeviceCode() {
        return mPreferences.getString(PREFERENCE_DEVICE_CODE, null);
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

    public long getNotificationTimeStamp() {
        return mPreferences.getLong(PREFERENCE_NOTIFICATION_TIMESTAMP, Long.MIN_VALUE);
    }

    public void setNotificationTimeStamp(long timestamp) {
        mPreferences.edit().putLong(PREFERENCE_NOTIFICATION_TIMESTAMP, timestamp).apply();
    }


    private String generatePrivateKey() {
        return new Wallet(MainNetParams.get())
                .getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION).getPrivateKeyAsWiF(MainNetParams.get());
    }

    public boolean isBackupInfoDialogShown() {
        return mPreferences.getBoolean(PREFERENCE_WALLET_BACKUP_SHOWN, false);
    }

    public void setBackupInfodialogShown(boolean shown) {
        mPreferences.edit().putBoolean(PREFERENCE_WALLET_BACKUP_SHOWN, shown).apply();
    }

    public long getLastSyncTime() {
        return mPreferences.getLong(PREFERENCE_KEY_LAST_SYNC_TIME, 0);
    }

    public void setLastSyncTime(long time) {
        mPreferences.edit().putLong(PREFERENCE_KEY_LAST_SYNC_TIME, time).apply();
    }

    public void setNewVersionLastScreenTime(long time) {
        mPreferences.edit().putLong(PREFERENCE_KEY_NEW_VERSION_LAST_SCREEN_TIME, time).apply();
    }

    public long getNewVersionLastScreenTime() {
        return mPreferences.getLong(PREFERENCE_KEY_NEW_VERSION_LAST_SCREEN_TIME, 0);
    }

    public void setNewVersionLastNotificationTime(long time) {
        mPreferences.edit().putLong(PREFERENCE_KEY_NEW_VERSION_LAST_NOTIFICATION_TIME, time).apply();
    }

    public long getNewVersionLastNotificationTime() {
        return mPreferences.getLong(PREFERENCE_KEY_NEW_VERSION_LAST_NOTIFICATION_TIME, 0);
    }

    public boolean isWalletBackedUp() {
        return mPreferences.getBoolean(PREFERENCE_KEY_IS_WALLET_BACKED_UP, false);
    }

    public void setWalletBackedUp(boolean value) {
        mPreferences.edit().putBoolean(PREFERENCE_KEY_IS_WALLET_BACKED_UP, value).apply();
    }

    public void setSwipeToVoteShown() {
        mPreferences.edit().putBoolean(PREFERENCE_KEY_SLIDE_TO_VOTE_SHOWN, true).apply();
    }

    public boolean isSwipeToVoteShown() {
        return mPreferences.getBoolean(PREFERENCE_KEY_SLIDE_TO_VOTE_SHOWN, false);
    }

    public boolean shallNotifyBackgroundAppRestrictions(int versionCode) {
        return mPreferences.getInt(PREFERENCE_KEY_BACKGROUND_APP_ACTIVITY, 0) < versionCode;
    }

    public void setNotifyBackgroundAppRestrictions(int versionCode) {
        mPreferences.edit().putInt(PREFERENCE_KEY_BACKGROUND_APP_ACTIVITY, versionCode).apply();
    }

    public boolean isNotifiedBackgroundAppRestrictions() {
        return mPreferences.getBoolean(PREFERENCE_KEY_IS_NOTIFIED_BACKGROUND_APP_ACTIVITY, false);
    }

    public void setNotifiedBackgroundAppRestrictions() {
        mPreferences.edit().putBoolean(PREFERENCE_KEY_IS_NOTIFIED_BACKGROUND_APP_ACTIVITY, true).apply();
    }

    public int getInfoMask(Context context) {
        boolean notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean powerSaveMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager != null && powerManager.isPowerSaveMode();
        return isDemoUser() ? 0 : (isWalletBackedUp() ? 16 : 0) | (notificationsEnabled ? 3 : 1) | (powerSaveMode ? 8 : 0);
    }


    public void updateLastBackupNotificationShown() {
        mPreferences.edit().putLong(PREFERENCE_KEY_LAST_BACK_UP_NOTIFICATION_SHOWN
                , System.currentTimeMillis()).apply();
    }


    public boolean canShowBackupNotification(long delay) {
        return Math.abs(mPreferences.getLong(PREFERENCE_KEY_LAST_BACK_UP_NOTIFICATION_SHOWN, 0) - System.currentTimeMillis())
                > delay;
    }

}
