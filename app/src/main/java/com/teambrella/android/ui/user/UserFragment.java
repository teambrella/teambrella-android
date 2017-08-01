package com.teambrella.android.ui.user;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.teammate.TeammateFragment;
import com.teambrella.android.ui.user.coverage.CoverageFragmnet;
import com.teambrella.android.ui.user.wallet.WalletFragment;

import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * User Fragment
 */
public class UserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/AkkuratPro-Bold.otf");
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        setTypeface(tabLayout, typeface);
        setTypeface(toolbar, typeface);

        toolbar.setTitle(R.string.profile);


        TeambrellaImageLoader.getInstance(getContext()).getPicasso().load(((IMainDataHost) getContext()).getTeamLogoUri())
                .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        toolbar.setLogo(new BitmapDrawable(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataProgressFragment.getInstance(MainActivity.USER_DATA, TeammateFragment.class);
                    case 1:
                        return new CoverageFragmnet();
                    case 2:
                        return new WalletFragment();
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.profile);
                    case 1:
                        return getString(R.string.coverage);
                    case 2:
                        return getString(R.string.wallet);
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        tabLayout.setupWithViewPager(pager);

        return view;
    }


    private void setTypeface(ViewGroup viewGroup, Typeface typeface) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(typeface);
            } else if (view instanceof ViewGroup) {
                setTypeface((ViewGroup) view, typeface);
            }
        }
    }
}

