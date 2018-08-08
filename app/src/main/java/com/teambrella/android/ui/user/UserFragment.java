package com.teambrella.android.ui.user;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.teammate.TeammateFragment;
import com.teambrella.android.ui.user.coverage.CoverageFragment;
import com.teambrella.android.ui.user.wallet.KWalletFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * User Fragment
 */
public class UserFragment extends AMainLandingFragment {

    private ViewPager mViewPager;
    private int mPendingItem = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        mViewPager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.USER_DATA, MainActivity.VOTE_DATA}, TeammateFragment.class);
                    case 1:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.WALLET_DATA}, CoverageFragment.class);
                    case 2:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.WALLET_DATA}, KWalletFragment.class);
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

        tabLayout.setupWithViewPager(mViewPager);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPendingItem >= 0) {
            mViewPager.setCurrentItem(mPendingItem);
            mPendingItem = -1;
        }
    }

    public void showCoverage() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(1);
        } else {
            mPendingItem = 1;
        }
    }

    public void showWallet() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(2);
        } else {
            mPendingItem = 2;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getDataHost().getTeamName());
    }

    private CharSequence getTitle(String title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(getContext()), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }
}

