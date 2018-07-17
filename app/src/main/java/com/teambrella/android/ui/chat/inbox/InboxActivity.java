package com.teambrella.android.ui.chat.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.services.push.INotificationMessage;
import com.teambrella.android.ui.base.ADataFragmentKt;
import com.teambrella.android.ui.base.ATeambrellaActivity;
import com.teambrella.android.ui.base.TeambrellaBroadcastManager;
import com.teambrella.android.ui.base.TeambrellaBroadcastReceiver;
import com.teambrella.android.ui.base.TeambrellaViewModelKt;

import org.jetbrains.annotations.NotNull;

import static com.teambrella.android.services.push.KPushNotifications.PRIVATE_MSG;

/**
 * Inbox Activity.
 */
public class InboxActivity extends ATeambrellaActivity {

    public static final String INBOX_DATA_TAG = "inbox_data_tag";
    public static final String INBOX_UI_TAG = "inbox_ui_tag";


    public static final int CONVERSATION_REQUEST_CODE = 111;


    private InboxNotificationClient mNotificationClient;
    private TeambrellaBroadcastManager mChatBroadcastManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        mChatBroadcastManager = new TeambrellaBroadcastManager(this);
        mChatBroadcastManager.registerReceiver(mChatBroadCastReceiver);

        setTitle(R.string.inbox);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(INBOX_UI_TAG) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ADataFragmentKt.createDataFragment(new String[]{INBOX_DATA_TAG}, InboxFragment.class), INBOX_UI_TAG)
                    .commit();
        }

        mNotificationClient = new InboxNotificationClient(this);
        mNotificationClient.connect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getPager(INBOX_DATA_TAG).reload();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected String[] getDataTags() {
        return new String[0];
    }

    @NonNull
    @Override
    protected String[] getDataPagerTags() {
        return new String[]{INBOX_DATA_TAG};
    }

    @Nullable
    @Override
    protected Bundle getDataPagerConfig(@NotNull String tag) {
        switch (tag) {
            case INBOX_DATA_TAG: {
                Bundle config = new Bundle();
                config.putParcelable(TeambrellaViewModelKt.EXTRA_URI, TeambrellaUris.getInbox());
                return config;
            }
        }
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationClient != null) {
            mNotificationClient.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNotificationClient != null) {
            mNotificationClient.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationClient.disconnect();
        mNotificationClient = null;
        mChatBroadcastManager.unregisterReceiver(mChatBroadCastReceiver);
    }

    private class InboxNotificationClient extends TeambrellaNotificationServiceClient {

        private boolean mResumed;
        private boolean mUpdateOnResume;

        InboxNotificationClient(Context context) {
            super(context);
        }

        @Override
        public boolean onPushMessage(INotificationMessage message) {
            switch (message.getCmd()) {
                case PRIVATE_MSG:
                    if (mResumed) {
                        getPager(INBOX_DATA_TAG).reload();
                    } else {
                        mUpdateOnResume = true;
                    }
            }
            return false;
        }

        void onPause() {
            mResumed = false;
        }

        void onResume() {
            mResumed = true;
            if (mUpdateOnResume) {
                getPager(INBOX_DATA_TAG).reload();
                mUpdateOnResume = false;
            }
        }
    }

    private TeambrellaBroadcastReceiver mChatBroadCastReceiver = new TeambrellaBroadcastReceiver() {
        @Override
        protected void onTopicRead(@NotNull String topicId) {
            super.onTopicRead(topicId);
            getPager(INBOX_DATA_TAG).reload();
        }
    };
}
