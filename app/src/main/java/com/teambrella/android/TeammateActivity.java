package com.teambrella.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Teammate screen.
 */
public class TeammateActivity extends AppCompatActivity {

    private static final String TEAMMATE_URI = "teammate_uri";


    @BindView(R.id.user_picture)
    ImageView mUserPicture;

    @BindView(R.id.user_name)
    TextView mUserName;


    private Uri mUri;


    private Unbinder mUnbinder;


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
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
