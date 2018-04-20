package com.teambrella.android.backup;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Teambrella Backup data
 */
public class TeambrellaBackupData {

    static final String NAME = "backup_data";

    private SharedPreferences mPrefs;
    private BackupManager mBackupManager;

    public TeambrellaBackupData(Context context) {
        mPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        mBackupManager = new BackupManager(context);
    }

    public void setValue(String key, String value) {
        mPrefs.edit().putString(key, value).apply();
        mBackupManager.dataChanged();
    }

    public String getValue(String key) {
        return mPrefs.getString(key, null);
    }
}
