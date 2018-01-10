package com.teambrella.android.image;

import android.net.Uri;

import com.teambrella.android.api.server.TeambrellaServer;

/**
 * Teambrella Image Loader
 */
public class TeambrellaImageLoader {

    private static Uri.Builder getUriBuilder() {
        return new Uri.Builder().scheme(TeambrellaServer.SCHEME).authority(TeambrellaServer.AUTHORITY);
    }

    public static Uri getImageUri(String path) {
        if (path == null) {
            return null;
        }
        return getUriBuilder().appendEncodedPath(path).build();
    }

}
