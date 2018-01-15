package com.teambrella.android.image;

import android.net.Uri;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.teambrella.android.api.server.TeambrellaServer;

/**
 * Teambrella Image Loader
 */
public class TeambrellaImageLoader {


    private final TeambrellaServer mServer;


    public TeambrellaImageLoader(TeambrellaServer teambrellaServer) {
        mServer = teambrellaServer;
    }


    private static Uri.Builder getUriBuilder() {
        return new Uri.Builder().scheme(TeambrellaServer.SCHEME).authority(TeambrellaServer.AUTHORITY);
    }

    public static Uri getImageUri(String path) {
        if (path == null) {
            return null;
        }
        return getUriBuilder().appendEncodedPath(path).build();
    }

    public GlideUrl getImageUrl(String path) {
        Uri uri = getImageUri(path);
        return new TeambrellaGlideUrl(uri.toString(), mServer.getHeaders());
    }

    private static class TeambrellaGlideUrl extends GlideUrl {

        TeambrellaGlideUrl(String url, Headers headers) {
            super(url, headers);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GlideUrl) {
                GlideUrl other = (GlideUrl) o;
                return getCacheKey().equals(other.getCacheKey());
            }
            return false;
        }
    }
}
