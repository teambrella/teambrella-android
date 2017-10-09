package com.teambrella.android.ui.chat;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

/**
 * Clam Chat Pager Fragment
 */
public class ChatPagerFragment extends TeambrellaDataPagerFragment {

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        ChatDataPagerLoader loader = new ChatDataPagerLoader(args.getParcelable(EXTRA_URI));
        ((TeambrellaDataHostActivity) getContext()).getComponent().inject(loader);
        return loader;
    }
}
