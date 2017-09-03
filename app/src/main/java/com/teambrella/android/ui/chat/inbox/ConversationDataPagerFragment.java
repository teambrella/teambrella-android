package com.teambrella.android.ui.chat.inbox;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.chat.ChatPagerFragment;

/**
 * Created by dvasilin on 03/09/2017.
 */

public class ConversationDataPagerFragment extends ChatPagerFragment {

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ConversationDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }
}
