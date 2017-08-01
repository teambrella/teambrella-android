package com.teambrella.android.ui.team;

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
import com.teambrella.android.ui.team.claims.ClaimsFragment;
import com.teambrella.android.ui.team.feed.FeedFragment;
import com.teambrella.android.ui.team.teammates.MembersFragment;

import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Team fragment
 */
public class TeamFragment extends Fragment {


    private static String EXTRA_TEAM_ID = "team_id";


    public static TeamFragment getInstance(int teamId) {
        TeamFragment fragment = new TeamFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_TEAM_ID, teamId);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/AkkuratPro-Bold.otf");
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        setTypeface(tabLayout, typeface);
        setTypeface(toolbar, typeface);

        toolbar.setTitle(getString(R.string.bottom_navigation_team));

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
                        return FeedFragment.getInstance(MainActivity.FEED_DATA_TAG, getArguments().getInt(EXTRA_TEAM_ID));
                    case 1:
                        return MembersFragment.getInstance(MainActivity.TEAMMATES_DATA_TAG, getArguments().getInt(EXTRA_TEAM_ID));
                    case 2:
                        return ClaimsFragment.getInstance(MainActivity.CLAIMS_DATA_TAG, getArguments().getInt(EXTRA_TEAM_ID));
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.team_feed);
                    case 1:
                        return getString(R.string.team_members);
                    case 2:
                        return getString(R.string.team_claims);
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
