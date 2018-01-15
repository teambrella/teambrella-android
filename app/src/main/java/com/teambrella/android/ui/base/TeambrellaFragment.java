package com.teambrella.android.ui.base;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.image.TeambrellaImageLoader;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Teambrella Fragment
 */
public class TeambrellaFragment extends Fragment {

    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mImageLoader;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ITeambrellaDaggerActivity) context).getComponent().inject(this);
    }

    protected TeambrellaImageLoader getImageLoader() {
        return mImageLoader;
    }

    protected ITeambrellaComponent getComponent() {
        return ((ITeambrellaDaggerActivity) getContext()).getComponent();
    }
}
