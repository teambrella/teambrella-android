package com.teambrella.android.ui.base;

import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.teambrella.android.dagger.Dependencies;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Teambrella Adapter
 */
public abstract class ATeambrellaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Inject
    @Named(Dependencies.PICASSO)
    Picasso mPicasso;


    protected Picasso getPicasso() {
        return mPicasso;
    }
}
