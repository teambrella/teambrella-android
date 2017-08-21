package com.teambrella.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Connectivity Utils
 */
public class ConnectivityUtils {

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo info = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return info != null && info.isConnected();
    }
}
