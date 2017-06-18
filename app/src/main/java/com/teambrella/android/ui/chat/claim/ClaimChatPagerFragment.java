package com.teambrella.android.ui.chat.claim;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Clam Chat Pager Fragment
 */
public class ClaimChatPagerFragment extends TeambrellaDataPagerFragment {

    @Override
    protected TeambrellaDataPagerLoader createLoader(Bundle args) {
        return new ClaimChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }
}
