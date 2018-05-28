package com.teambrella.android.ui.withdraw;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

import java.util.Objects;

/**
 * Withdrawals Data Pager Fragment
 */
public class WithdrawalsDataPagerFragment extends TeambrellaDataPagerFragment {
    @NonNull
    @Override
    protected IDataPager<JsonArray> createLoader(@NonNull Bundle args) {
        final Uri uri = args.getParcelable(EXTRA_URI);
        final String property = args.getString(EXTRA_PROPERTY);
        WithdrawalsDataPagerLoader loader = new WithdrawalsDataPagerLoader(uri, property);
        ((TeambrellaDataHostActivity) Objects.requireNonNull(getContext())).getComponent().inject(loader);
        return loader;
    }
}
