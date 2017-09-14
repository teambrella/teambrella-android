package com.teambrella.android.image;

import android.content.Context;
import android.net.Uri;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

/**
 * Teambrella Image Loader
 */
public class TeambrellaImageLoader {

    private static TeambrellaImageLoader sInstance;

    private Picasso mPicasso;

    public static synchronized TeambrellaImageLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TeambrellaImageLoader(context, new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey()));
        }
        return sInstance;
    }


    private TeambrellaImageLoader(Context context, TeambrellaServer server) {
        mPicasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(server.getHttpClient(context))).loggingEnabled(BuildConfig.DEBUG)
                .listener((picasso, uri, exception) -> {
                }).build();
    }


    public Picasso getPicasso() {
        return mPicasso;
    }


    public static Uri.Builder getUriBuilder() {
        return new Uri.Builder().scheme(TeambrellaServer.SCHEME).authority(TeambrellaServer.AUTHORITY);
    }

    public static Uri getImageUri(String path) {
        if (path == null) {
            return null;
        }
        return getUriBuilder().appendEncodedPath(path).build();
    }

}
