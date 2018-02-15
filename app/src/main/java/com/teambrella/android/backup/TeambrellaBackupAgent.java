package com.teambrella.android.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Teambrella Backup Agent
 */
public class TeambrellaBackupAgent extends BackupAgentHelper {

    private static final String PREFS_BACKUP_KEY = "tbd";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, TeambrellaBackupData.NAME);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
