package com.teambrella.android.ui.chat.claim;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.widget.TextView;

import com.teambrella.android.R;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Claim chat
 */
public class ClaimChatActivity extends ADataHostActivity {

    private static final String EXTRA_CLAIM_ID = "claim_id";
    private static final String EXTRA_TOPIC_ID = "topicId";


    private static final String DATA_FRAGMENT_TAG = "data_fragment_tag";
    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";


    private int mClaimId;
    private String mTopicId;
    private Handler mHandler = new Handler();


    public static Intent getLaunchIntent(Context context, int claimId, String topicId) {
        return new Intent(context, ClaimChatActivity.class)
                .putExtra(EXTRA_CLAIM_ID, claimId).putExtra(EXTRA_TOPIC_ID, topicId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mClaimId = getIntent().getIntExtra(EXTRA_CLAIM_ID, 0);
        mTopicId = getIntent().getStringExtra(EXTRA_TOPIC_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_chat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        }
        setTitle(getString(R.string.claim_title_format_string, getIntent().getIntExtra(EXTRA_CLAIM_ID, 0)));

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(UI_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, ClaimChatFragment.getInstance(DATA_FRAGMENT_TAG, ClaimChatFragment.class), UI_FRAGMENT_TAG)
                    .commit();
        }

        findViewById(R.id.send_text).setOnClickListener(v -> {
            TextView textView = (TextView) findViewById(R.id.text);
            TeambrellaServer server = new TeambrellaServer(ClaimChatActivity.this, TeambrellaUser.get(ClaimChatActivity.this).getPrivateKey());
            server.requestObservable(TeambrellaUris.getNewPostUri(mTopicId, textView.getText().toString()), null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(jsonObject -> {
                        textView.setText(null);
                        getPager(DATA_FRAGMENT_TAG).loadNext(true);
                    }, throwable -> {
                        textView.setText(null);
                        getPager(DATA_FRAGMENT_TAG).loadNext(true);
                    });

        });

        mHandler.postDelayed(mUpdate, 4000);
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
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{DATA_FRAGMENT_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }


    public void update() {
        //getPager(DATA_FRAGMENT_TAG).loadNext(true);
        //mHandler.postDelayed(mUpdate, 2000);
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT_TAG:
                return ClaimChatPagerFragment.getInstance(TeambrellaUris.getClaimChatUri(mClaimId), null, ClaimChatPagerFragment.class);
        }
        return null;
    }

    @Override
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdate);
    }


    private Runnable mUpdate = () -> update();
}
