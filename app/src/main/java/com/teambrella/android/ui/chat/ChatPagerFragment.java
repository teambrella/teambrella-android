package com.teambrella.android.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.services.TeambrellaNotificationService;

/**
 * Clam Chat Pager Fragment
 */
public class ChatPagerFragment extends TeambrellaDataPagerFragment {

    private NewMessageBroadcastReceiver mNewMessageBroadcastReceiver;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mNewMessageBroadcastReceiver == null) {
            mNewMessageBroadcastReceiver = new NewMessageBroadcastReceiver();
            LocalBroadcastManager.getInstance(context).registerReceiver(mNewMessageBroadcastReceiver
                    , new IntentFilter(TeambrellaNotificationService.ON_NEW_MESSAGE_RECEIVED));
        }
    }

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }


    @Override
    public void onDetach() {
        if (getActivity().isFinishing()) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mNewMessageBroadcastReceiver);
        }
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getPager().loadNext(true);
        }
    }
}
