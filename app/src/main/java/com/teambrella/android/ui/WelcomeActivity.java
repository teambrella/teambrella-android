package com.teambrella.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

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
        View tryDemoView = findViewById(R.id.try_demo);
        if (BuildConfig.DEBUG) {
            tryDemoView.setOnClickListener(this::onTryDemo);
        } else {
            tryDemoView.setVisibility(View.INVISIBLE);
            tryDemoView.setEnabled(false);
        }
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
                String userId = loginResult.getAccessToken().getUserId();
                String privateKey = null;
                switch (userId) {
                    case BuildConfig.DENIS_FACEEBOOK_ID:
                        privateKey = BuildConfig.DENIS_PRIVATE_KEY;
                        break;
                    case BuildConfig.KATE_FACEEBOOK_ID:
                        privateKey = BuildConfig.KATE_PRIVATE_KEY;
                        break;
                    case BuildConfig.THORAX_FACEEBOOK_ID:
                        privateKey = BuildConfig.THORAX_PRIVATE_KEY;
                        break;
                    case BuildConfig.EUGENE_FACEEBOOK_ID:
                        privateKey = BuildConfig.EUGENE_PRIVATE_KEY;
                        break;
                    case BuildConfig.STASIA_FACEBOOK_ID:
                        privateKey = BuildConfig.STASIA_PRIVATE_KEY;
                        break;
                    case BuildConfig.VLAD_FACEBOOK_ID:
                        privateKey = BuildConfig.VLAD_PRIVATE_KEY;
                        break;
                    default:
                        Toast.makeText(WelcomeActivity.this, "Unknown user", Toast.LENGTH_LONG).show();
                        break;
                }


                if (privateKey != null) {
                    TeambrellaUser.get(WelcomeActivity.this).setPrivateKey(privateKey);
                    getTeams(privateKey);
                }

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
        String key = new Wallet(MainNetParams.get())
                .getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION).getPrivateKeyAsWiF(MainNetParams.get());
        new TeambrellaServer(this, key).requestObservable(TeambrellaUris.getRegisterUri(token), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject -> {
                        }
                        , throwable -> {
                        });
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
