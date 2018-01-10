package com.teambrella.android.ui.base;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.squareup.picasso.Picasso;
import com.teambrella.android.dagger.Dependencies;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Teambrella Dialog Fragment
 */
public class ATeambrellaDialogFragment extends DialogFragment {
    @Inject
    @Named(Dependencies.PICASSO)
    Picasso mPicasso;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ITeambrellaDaggerActivity) context).getComponent().inject(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mPicasso = null;
    }

    protected Picasso getPicasso() {
        return mPicasso;
    }
}
