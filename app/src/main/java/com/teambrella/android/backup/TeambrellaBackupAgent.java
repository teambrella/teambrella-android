package com.teambrella.android.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import com.teambrella.android.util.StatisticHelper;

import java.io.IOException;

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

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);
        StatisticHelper.onBackUp();
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        StatisticHelper.onRestore();
    }
}
