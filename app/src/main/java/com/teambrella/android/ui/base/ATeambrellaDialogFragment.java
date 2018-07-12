package com.teambrella.android.ui.base;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Teambrella Dialog Fragment
 */
public class ATeambrellaDialogFragment extends DialogFragment {
    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mTeambrellaImageLoader;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ATeambrellaDaggerActivity) context).getComponent().inject(this);
    }

    protected TeambrellaImageLoader getTeambrellaImageLoader() {
        return mTeambrellaImageLoader;
    }
}
