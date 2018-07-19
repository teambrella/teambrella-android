package com.teambrella.android.ui.team;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.team.claims.TeamClaimsFragment;
import com.teambrella.android.ui.team.feed.FeedFragment;
import com.teambrella.android.ui.team.teammates.MembersFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * Team fragment
 */
public class TeamFragment extends AMainLandingFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/AkkuratPro-Bold.otf");
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        ViewPager pager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        setTypeface(tabLayout, typeface);

        pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.FEED_DATA_TAG}, FeedFragment.class);
                    case 1:
                        return MembersFragment.getInstance(MainActivity.TEAMMATES_DATA_TAG, getDataHost().getTeamId());
                    case 2:
                        return TeamClaimsFragment.getInstance(MainActivity.CLAIMS_DATA_TAG, getDataHost().getTeamId(), ((IMainDataHost) getContext()).getCurrency());
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getTitle(getString(R.string.team_feed));
                    case 1:
                        return getTitle(getString(R.string.team_members));
                    case 2:
                        return getTitle(getString(R.string.team_claims));
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getDataHost().getTeamName());
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

    private CharSequence getTitle(String title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(getContext()), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }

}
