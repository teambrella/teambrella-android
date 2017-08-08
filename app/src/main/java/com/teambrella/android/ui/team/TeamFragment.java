package com.teambrella.android.ui.team;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        ViewPager pager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        setTypeface(tabLayout, typeface);

        IMainDataHost dataHost = (IMainDataHost) getContext();

        TeambrellaImageLoader.getInstance(getContext()).getPicasso().load(dataHost.getTeamLogoUri())
                .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask))
                .into((ImageView) view.findViewById(R.id.team_logo));

        ((TextView) view.findViewById(R.id.title)).setText(dataHost.getTeamName());

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
