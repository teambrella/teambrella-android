package com.teambrella.android.image.glide;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.teambrella.android.BuildConfig;

/**
 * Teambrella Glide Module
 */
@GlideModule
public class TeambrellaGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setLogLevel(BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR).setDecodeFormat(DecodeFormat.PREFER_RGB_565)
                .setDefaultRequestOptions(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL));
    }
}
