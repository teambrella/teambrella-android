package com.teambrella.android.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.data.base.TeambrellaRequestFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.ImagePicker;

import java.io.File;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Claim chat
 */
public class ChatActivity extends ADataHostActivity {

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_TOPIC_ID = "topicId";


    private static final String DATA_FRAGMENT_TAG = "data_fragment_tag";
    private static final String UI_FRAGMENT_TAG = "ui_fragment_tag";
    private static final String DATA_REQUEST_FRAGMENT_TAG = "data_request";


    private Uri mUri;
    private String mTopicId;
    private Disposable mDisposable;


    private TextView mMessageView;
    private ImagePicker mImagePicker;


    public static Intent getLaunchIntent(Context context, Uri uri, String topicId) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_URI, uri).putExtra(EXTRA_TOPIC_ID, topicId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mUri = getIntent().getParcelableExtra(EXTRA_URI);
        mTopicId = getIntent().getStringExtra(EXTRA_TOPIC_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_chat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
        }

        mImagePicker = new ImagePicker(this);

        //setTitle(getString(R.string.claim_title_format_string, getIntent().getIntExtra(EXTRA_CLAIM_ID, 0)));

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(UI_FRAGMENT_TAG) == null) {
            transaction.add(R.id.container, ChatFragment.getInstance(DATA_FRAGMENT_TAG, ChatFragment.class), UI_FRAGMENT_TAG);
        }


        if (fragmentManager.findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG) == null) {
            transaction.add(new TeambrellaRequestFragment(), DATA_REQUEST_FRAGMENT_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }


        mMessageView = findViewById(R.id.text);
        findViewById(R.id.send_text).setOnClickListener(this::onClick);
        findViewById(R.id.send_image).setOnClickListener(this::onClick);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            mDisposable = fragment.getObservable().subscribe(this::onRequestResult);
            fragment.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.stop();
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        mDisposable = null;
    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_text:
                request(TeambrellaUris.getNewPostUri(mTopicId, mMessageView.getText().toString(), null));
                break;
            case R.id.send_image:
                mImagePicker.startPicking();
                break;
        }
    }


    public void request(Uri uri) {
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.request(uri);
        }
    }


    private void onRequestResult(Notification<JsonObject> response) {
        if (response.isOnNext()) {
            String requestUriString = Observable.just(response.getValue()).map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI))
                    .blockingFirst(null);
            switch (TeambrellaUris.sUriMatcher.match(Uri.parse(requestUriString))) {
                case TeambrellaUris.NEW_FILE:
                    JsonArray array = Observable.just(response.getValue()).map(JsonWrapper::new)
                            .map(jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA)).blockingFirst();
                    request(TeambrellaUris.getNewPostUri(mTopicId, null, array.toString()));
                    break;
                case TeambrellaUris.NEW_POST:
                    mMessageView.setText(null);
                    getPager(DATA_FRAGMENT_TAG).loadNext(true);
                    break;
            }
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Observable<File> result = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            result.subscribe(file -> request(TeambrellaUris.getNewFileUri(file.getAbsolutePath())),
                    throwable -> {
                    });
        }
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


    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case DATA_FRAGMENT_TAG:
                return ChatPagerFragment.getInstance(mUri, null, ChatPagerFragment.class);
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
}
