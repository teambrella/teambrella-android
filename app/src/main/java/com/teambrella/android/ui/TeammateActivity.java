package com.teambrella.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.TeambrellaDataFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

/**
 * Teammate screen.
 */
public class TeammateActivity extends AppCompatActivity {

    private static final String TEAMMATE_URI = "teammate_uri";

    private static final String DATA_FRAGMENT = "data";


    @BindView(R.id.user_picture)
    ImageView mUserPicture;

    @BindView(R.id.user_name)
    TextView mUserName;


    private Uri mUri;


    private Unbinder mUnbinder;


    private Disposable mDisposable;


    /**
     * Get intent to launch activity
     *
     * @param context to use
     * @param uri     teammate uri
     * @return intent to start activity
     */
    public static Intent getIntent(Context context, Uri uri) {
        return new Intent(context, TeammateActivity.class).putExtra(TEAMMATE_URI, uri);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_teammate);
        mUri = getIntent().getParcelableExtra(TEAMMATE_URI);
        mUnbinder = ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction()
                .add(TeambrellaDataFragment.getInstance(mUri), DATA_FRAGMENT).commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            TeambrellaDataFragment fragment = (TeambrellaDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAGMENT);
            if (fragment != null) {
                fragment.load();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        TeambrellaDataFragment fragment = (TeambrellaDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAGMENT);
        if (fragment != null) {
            mDisposable = fragment.getObservable().subscribe(this::onResult, this::onError, this::onComplete);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private void onResult(JsonObject response) {
        JsonObject data = response.get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
        Picasso.with(this).load(TeambrellaServer.AUTHORITY + data.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                .into((ImageView) findViewById(R.id.user_picture));
    }

    private void onError(Throwable e) {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    private void onComplete() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
