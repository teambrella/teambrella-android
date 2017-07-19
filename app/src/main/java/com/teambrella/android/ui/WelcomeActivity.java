package com.teambrella.android.ui;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

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

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        TeambrellaUser user = TeambrellaUser.get(this);
        setContentView(R.layout.acivity_welcome);
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        LinkedList<String> permission = new LinkedList<>();
        permission.add("public_profile");
        permission.add("email");
        loginButton.setReadPermissions(permission);
        loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String key = new Wallet(new MainNetParams())
                        .getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION).getPrivateKeyAsWiF(new MainNetParams());
                new RegisterKeyTask(key, loginResult.getAccessToken().getToken()).execute();
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
        findViewById(R.id.masterUser).setOnClickListener(mClickListener);


        if (user.getPrivateKey() != null) {
//            PeriodicTask task = new PeriodicTask.Builder()
//                    .setService(TeambrellaUtilService.class)
//                    .setTag("account_task")
//                    .setPeriod(10L)
//                    .build();
//
//            GcmNetworkManager.getInstance(WelcomeActivity.this).schedule(task);
            loginButton.postDelayed(() -> new GetTeamIDTask().execute(user.getPrivateKey()), 2000);
        } else {
            findViewById(R.id.denis).setVisibility(View.VISIBLE);
            findViewById(R.id.kate).setVisibility(View.VISIBLE);
            findViewById(R.id.thorax).setVisibility(View.VISIBLE);
            findViewById(R.id.masterUser).setVisibility(View.VISIBLE);

            findViewById(R.id.denis).setOnClickListener(mClickListener);
            findViewById(R.id.kate).setOnClickListener(mClickListener);
            findViewById(R.id.thorax).setOnClickListener(mClickListener);
            findViewById(R.id.masterUser).setOnClickListener(mClickListener);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener mClickListener = v -> {
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
            case R.id.masterUser:
                user.setPrivateKey(BuildConfig.MASTER_USER_PRIVATE_KEY);
                break;
        }
        new GetTeamIDTask().execute(user.getPrivateKey());
    };

    private class GetTeamIDTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.denis).setVisibility(View.GONE);
            findViewById(R.id.kate).setVisibility(View.GONE);
            findViewById(R.id.thorax).setVisibility(View.GONE);
            findViewById(R.id.masterUser).setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(String... keys) {
            TeambrellaServer server = new TeambrellaServer(WelcomeActivity.this, keys[0]);
            return server.requestObservable(TeambrellaUris.getMyTeams(), null).map(jsonObject -> new JsonWrapper(jsonObject)
                    .getObject(TeambrellaModel.ATTR_DATA)
                    .getArray(TeambrellaModel.ATTR_DATA_MY_TEAMS)
                    .get(0).getInt(TeambrellaModel.ATTR_DATA_TEAM_ID, 0)).blockingFirst();
        }

        @Override
        protected void onPostExecute(Integer teamId) {
            super.onPostExecute(teamId);
            startActivity(MainActivity.getLaunchIntent(WelcomeActivity.this, teamId));
            finish();
        }
    }


    private class RegisterKeyTask extends AsyncTask<Void, Void, Boolean> {

        private final String mKey;
        private final String mFacebookToken;

        public RegisterKeyTask(String key, String facebookToken) {
            mKey = key;
            mFacebookToken = facebookToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            TeambrellaServer server = new TeambrellaServer(WelcomeActivity.this, mKey);

            //try {
            server.requestObservable(TeambrellaUris.getRegisterUri(mFacebookToken), null);
            //} catch (TeambrellaException e) {
            //    Log.e(LOG_TAG, e.toString());
            //    return false;
            //}

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                TeambrellaUser.get(WelcomeActivity.this).setPrivateKey(mKey);
                finish();
            }
        }
    }

}
