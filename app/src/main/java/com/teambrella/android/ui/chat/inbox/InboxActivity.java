package com.teambrella.android.ui.chat.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.services.TeambrellaNotificationServiceClient;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;

/**
 * Inbox Activity.
 */
public class InboxActivity extends ADataHostActivity {

    public static final String INBOX_DATA_TAG = "inbox_data_tag";
    public static final String INBOX_UI_TAG = "inbox_ui_tag";


    public static final int CONVERSATION_REQUEST_CODE = 111;


    private InboxNotificationClient mNotificationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        setTitle(R.string.inbox);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(INBOX_UI_TAG) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ADataPagerProgressFragment.getInstance(INBOX_DATA_TAG, InboxFragment.class), INBOX_UI_TAG)
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

    @Override
    protected String[] getDataTags() {
        return new String[0];
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{INBOX_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case INBOX_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getInbox(), null, TeambrellaDataPagerFragment.class);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationClient.disconnect();
        mNotificationClient = null;
    }

    private class InboxNotificationClient extends TeambrellaNotificationServiceClient {

        InboxNotificationClient(Context context) {
            super(context);
        }

        @Override
        public boolean onPrivateMessage(String userId, String name, String avatar, String text) {
            getPager(INBOX_DATA_TAG).reload();
            return false;
        }
    }
}
