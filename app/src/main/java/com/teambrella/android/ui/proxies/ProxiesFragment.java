package com.teambrella.android.ui.proxies;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambrella.android.R;
import com.teambrella.android.ui.AMainLandingFragment;
import com.teambrella.android.ui.MainActivity;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.proxies.myproxies.MyProxiesFragment;
import com.teambrella.android.ui.proxies.proxyfor.ProxyForFragment;
import com.teambrella.android.ui.proxies.userating.UserRatingFragment;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * Proxies
 */
public class ProxiesFragment extends AMainLandingFragment {

    private ViewPager mViewPager;
    private int mPendingItem = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxies, container, false);
        mViewPager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.proxy_vote));

        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.MY_PROXIES_DATA}, MyProxiesFragment.class);
                    case 1:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.PROXIES_FOR_DATA}, ProxyForFragment.class);
                    case 2:
                        return ADataFragmentKt.createDataFragment(new String[]{MainActivity.USER_RATING_DATA}, UserRatingFragment.class);
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getTitle(getString(R.string.my_proxies), false);
                    case 1:
                        return getTitle(getString(R.string.proxy_for), false);
                    case 2:
                        return getTitle(getString(R.string.rating), false);
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

    public void showIAmProxyFor() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(1);
        } else {
            mPendingItem = 1;
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

    private CharSequence getTitle(String title, boolean attention) {
        SpannableString s = new SpannableString(title + (attention ? " •" : ""));
        s.setSpan(new AkkuratBoldTypefaceSpan(getContext()), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (attention) {
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.tealish)), title.length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new CurrencyRelativeSizeSpan("1234567890"), title.length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return s;
    }


    private static class CurrencyRelativeSizeSpan extends MetricAffectingSpan {


        private final String mText;

        CurrencyRelativeSizeSpan(String text) {
            mText = text;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            updateAnyState(ds);
        }

        @Override
        public void updateMeasureState(TextPaint ds) {
            updateAnyState(ds);
        }

        private void updateAnyState(TextPaint ds) {
            Rect bounds = new Rect();
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            int shift = bounds.top - bounds.bottom;
            ds.setTextSize(ds.getTextSize() * 1.5f);
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            shift += (bounds.bottom - bounds.top) / 2;
            ds.baselineShift += (shift + 2);
        }
    }
}
