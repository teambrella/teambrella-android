package com.teambrella.android.ui.chat.claim;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

import com.teambrella.android.R;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;

/**
 * Claim chat
 */
public class ClaimChatActivity extends ADataHostActivity {

    private static final String EXTRA_CLAIM_ID = "claim_id";


    public static Intent getLaunchIntent(Context context, int claimId) {
        return new Intent(context, ClaimChatActivity.class)
                .putExtra(EXTRA_CLAIM_ID, claimId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        }
        setTitle(getString(R.string.claim_title_format_string, getIntent().getIntExtra(EXTRA_CLAIM_ID, 0)));
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
        return new String[0];
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        return null;
    }

    @Override
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }
}
