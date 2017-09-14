package com.teambrella.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.EtherAccount;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

import java.util.LinkedList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Wellcome activity
 */
public class WelcomeActivity extends AppCompatActivity {

    private CallbackManager mCallBackManager = CallbackManager.Factory.create();

    private Disposable mTeamsDisposal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        TeambrellaUser user = TeambrellaUser.get(this);
        setContentView(R.layout.acivity_welcome);
        findViewById(R.id.facebook_login).setOnClickListener(this::onFacebookLogin);
        findViewById(R.id.try_demo).setOnClickListener(this::onTryDemo);
        if (user.getPrivateKey() != null) {
            getTeams(user.getPrivateKey());
        }
    }


    private void getTeams(String privateKey) {
        findViewById(R.id.facebook_login).setVisibility(View.GONE);
        findViewById(R.id.try_demo).setVisibility(View.GONE);
        TeambrellaUser user = TeambrellaUser.get(this);
        final int selectedTeam = TeambrellaUser.get(this).getTeamId();
        mTeamsDisposal = new TeambrellaServer(WelcomeActivity.this, privateKey)
                .requestObservable(TeambrellaUris.getMyTeams(), null)
                .map(JsonWrapper::new)
                .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(jsonWrapper -> user.setUserId(jsonWrapper.getString(TeambrellaModel.ATTR_DATA_USER_ID)))
                .map(jsonWrapper -> jsonWrapper.getArray(TeambrellaModel.ATTR_DATA_MY_TEAMS))
                .subscribe(teams -> {

                            JsonWrapper team = null;
                            for (JsonWrapper aTeam : teams) {
                                if (aTeam.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID) == selectedTeam) {
                                    team = aTeam;
                                    break;
                                }
                            }

                            if (team == null && teams.size() > 0) {
                                team = teams.get(0);
                            }

                            if (team == null) {
                                finish();
                                return;
                            }

                            startActivity(MainActivity.getLaunchIntent(WelcomeActivity.this
                                    , user.getUserId()
                                    , team.getObject().toString()));

                            user.setTeamId(team.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
                            finish();
                        }
                        , e -> {
                            Toast.makeText(this, "Something Went Wrong ", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.facebook_login).setVisibility(View.VISIBLE);
                            findViewById(R.id.try_demo).setVisibility(View.VISIBLE);
                        }
                );
    }


    private void onFacebookLogin(View v) {
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                String userId = token.getUserId();
                TeambrellaUser u = TeambrellaUser.get(WelcomeActivity.this);
                String privateKey = u.getPrivateKey();
                if (privateKey != null) {
                    getTeams(privateKey);
                }else{
                    privateKey = new Wallet(MainNetParams.get())
                            .getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION).getPrivateKeyAsWiF(MainNetParams.get());
                    u.setPrivateKey(privateKey);
                }

                registerUser(token.getToken());
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(WelcomeActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });

        LinkedList<String> permissions = new LinkedList<>();
        permissions.add("public_profile");
        permissions.add("email");
        loginManager.logInWithReadPermissions(this, permissions);
    }


    private void registerUser(String token) {
        String key = TeambrellaUser.get(this).getPrivateKey();
        String publicKeySignature = EtherAccount.getPublicKeySignature(key, getApplicationContext());

        new TeambrellaServer(this, key).requestObservable(TeambrellaUris.getRegisterUri(token, publicKeySignature), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject -> Log.e("TEST", key)
                        , throwable -> Log.e("TEST", throwable.toString()));
        Log.e("TEST", key);
    }
    

    private void onTryDemo(View v) {
        TeambrellaUser.get(this).setPrivateKey(BuildConfig.MASTER_USER_PRIVATE_KEY);
        getTeams(BuildConfig.MASTER_USER_PRIVATE_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTeamsDisposal != null && !mTeamsDisposal.isDisposed()) {
            mTeamsDisposal.dispose();
        }
    }
}
