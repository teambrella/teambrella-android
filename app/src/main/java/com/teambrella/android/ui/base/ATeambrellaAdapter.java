package com.teambrella.android.ui.base;

import android.support.v7.widget.RecyclerView;

import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.image.TeambrellaImageLoader;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Teambrella Adapter
 */
public abstract class ATeambrellaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mImageLoader;

    protected TeambrellaImageLoader getImageLoader() {
        return mImageLoader;
    }
}
