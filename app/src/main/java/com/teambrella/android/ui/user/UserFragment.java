package com.teambrella.android.ui.user;

import android.os.Bundle;
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

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.teammate.TeammateFragment;
import com.teambrella.android.ui.user.coverage.CoverageFragment;
import com.teambrella.android.ui.user.wallet.WalletFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * User Fragment
 */
public class UserFragment extends AMainLandingFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ViewPager pager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataProgressFragment.getInstance(new String[]{MainActivity.USER_DATA, MainActivity.VOTE_DATA}, TeammateFragment.class);
                    case 1:
                        return ADataFragment.getInstance(MainActivity.WALLET_DATA, CoverageFragment.class);
                    case 2:
                        return ADataProgressFragment.getInstance(MainActivity.WALLET_DATA, WalletFragment.class);
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getTitle(getString(R.string.profile));
                    case 1:
                        return getTitle(getString(R.string.coverage));
                    case 2:
                        return getTitle(getString(R.string.wallet));
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(mDataHost.getTeamName());
    }

    private CharSequence getTitle(String title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(getContext()), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }
}

