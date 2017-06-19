package com.teambrella.android.ui.chat.claim;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;

/**
 * Clam Chat Pager Fragment
 */
public class ClaimChatPagerFragment extends TeambrellaDataPagerFragment {

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ClaimChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }
}
