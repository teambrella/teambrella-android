@file:JvmName("BackgroundUtils")

package com.teambrella.android.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserManager
import com.teambrella.android.util.log.Log

private const val HUAWEI_SYSTEM_MANAGER = "com.huawei.systemmanager"
private const val HUAWEI_PROTECT_ACTIVITY = "com.huawei.systemmanager.optimize.process.ProtectActivity"
private const val HUAWEI_STARTUP_ACTIVITY = "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
private const val HUAWEI_PROTECT_COMMAND_LINE = "am start -n com.huawei.systemmanager/.optimize.process.ProtectActivity"

private const val LOG_TAG = "BackgroundUtils"

const val VERSION_CODE = 1

val Context.isHuaweiProtectedAppAvailable: Boolean
    get() {
        val activities = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
                .firstOrNull { it.packageName == HUAWEI_SYSTEM_MANAGER }
                ?.activities
                ?.filter {
                    it.name == HUAWEI_PROTECT_ACTIVITY
                            || it.name == HUAWEI_STARTUP_ACTIVITY
                }

        return activities?.firstOrNull { it.name == HUAWEI_PROTECT_ACTIVITY } != null
                && activities.firstOrNull { it.name == HUAWEI_STARTUP_ACTIVITY } == null

    }


fun Context.startHuaweiProtectApp(): Boolean {

    return try {
        var command = HUAWEI_PROTECT_COMMAND_LINE
        val userSerial = (this.getSystemService(Context.USER_SERVICE) as UserManager)
                .getSerialNumberForUser(Process.myUserHandle())
        command += " --user $userSerial"
        Runtime.getRuntime().exec(command);
        true
    } catch (e: Exception) {
        Log.e(LOG_TAG, "unable to start protect activity", e)
        false
    }

}
