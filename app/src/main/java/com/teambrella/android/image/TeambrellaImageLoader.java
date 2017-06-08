package com.teambrella.android.image;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.ui.TeambrellaUser;

/**
 * Teambrella Image Loader
 */
public class TeambrellaImageLoader {

    private static TeambrellaImageLoader sInstance;

    private Picasso mPicasso;
    private TeambrellaServer mServer;

    public static synchronized TeambrellaImageLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TeambrellaImageLoader(context, new TeambrellaServer(context, TeambrellaUser.get(context).getPrivateKey()));
        }
        return sInstance;
    }


    private TeambrellaImageLoader(Context context, TeambrellaServer server) {
        mServer = server;
        mPicasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(server.getHttpClient())).loggingEnabled(true).build();
    }


    public Picasso getPicasso() {
        return mPicasso;
    }

}
