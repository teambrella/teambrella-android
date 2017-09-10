package com.teambrella.android.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

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
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TeambrellaNotificationService.ON_CREATED_POST);
            intentFilter.addAction(TeambrellaNotificationService.ON_DELETED_POST);
            intentFilter.addAction(TeambrellaNotificationService.ON_PRIVATE_MSG);
            context.registerReceiver(mNewMessageBroadcastReceiver
                    , intentFilter);
        }
    }

    @Override
    protected IDataPager<JsonArray> createLoader(Bundle args) {
        return new ChatDataPagerLoader(getContext(), args.getParcelable(EXTRA_URI));
    }


    @Override
    public void onDetach() {
        if (getActivity().isFinishing()) {
            getContext().unregisterReceiver(mNewMessageBroadcastReceiver);
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
