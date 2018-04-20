package com.teambrella.android.ui.proxies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.ui.proxies.myproxies.MyProxiesFragment;
import com.teambrella.android.ui.proxies.proxyfor.ProxyForFragment;
import com.teambrella.android.ui.proxies.userating.UserRatingFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * Proxies
 */
public class ProxiesFragment extends AMainLandingFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxies, container, false);
        ViewPager pager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.proxy_vote));

        pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataPagerProgressFragment.getInstance(MainActivity.MY_PROXIES_DATA, MyProxiesFragment.class);
                    case 1:
                        return ADataPagerProgressFragment.getInstance(MainActivity.PROXIES_FOR_DATA, ProxyForFragment.class);
                    case 2:
                        return ADataPagerProgressFragment.getInstance(MainActivity.USER_RATING_DATA, UserRatingFragment.class);
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getTitle(getString(R.string.my_proxies));
                    case 1:
                        return getTitle(getString(R.string.proxy_for));
                    case 2:
                        return getTitle(getString(R.string.rating));
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
