package com.teambrella.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.LinkedList;

/**
 * Wellcome activity
 */
public class WelcomeActivity extends AppCompatActivity {


    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private CallbackManager mCallBackManager = CallbackManager.Factory.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TeambrellaUser.get(this).getPrivateKey() != null) {
            startActivity(new Intent(this, TeamActivity.class));
            finish();
        }


        setContentView(R.layout.acivity_welcome);
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        LinkedList<String> permission = new LinkedList<>();
        permission.add("public_profile");
        permission.add("email");
        loginButton.setReadPermissions(permission);
        loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(LOG_TAG, "onSuccess");
                Log.v(LOG_TAG, loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Log.v(LOG_TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.v(LOG_TAG, "onError " + error);
            }
        });

        findViewById(R.id.denis).setOnClickListener(mClickListener);
        findViewById(R.id.kate).setOnClickListener(mClickListener);
        findViewById(R.id.thorax).setOnClickListener(mClickListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TeambrellaUser user = TeambrellaUser.get(WelcomeActivity.this);
            switch (v.getId()) {
                case R.id.denis:
                    user.setPrivateKey(BuildConfig.DENIS_PRIVATE_KEY);
                    break;
                case R.id.kate:
                    user.setPrivateKey(BuildConfig.KATE_PRIVATE_KEY);
                    break;
                case R.id.thorax:
                    user.setPrivateKey(BuildConfig.THORAX_PRIVATE_KEY);
                    break;
            }

            startActivity(new Intent(WelcomeActivity.this, TeamActivity.class));
            finish();
        }
    };
}
