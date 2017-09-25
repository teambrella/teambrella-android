package com.teambrella.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.ui.base.AppCompatRequestActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import io.reactivex.Notification;

/**
 * Wellcome activity
 */
public class WelcomeActivity extends AppCompatRequestActivity {

    private enum State {
        INIT,
        LOADING,
        INVITE_ONLY
    }


    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();
    private CallbackManager mCallBackManager = CallbackManager.Factory.create();
    private View mInvitationOnlyView;
    private View mFacebookLoginButton;
    private View mTryDemoButton;
    private TeambrellaUser mUser;
    private State mState;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = TeambrellaUser.get(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.acivity_welcome);

        mInvitationOnlyView = findViewById(R.id.invitation_only);
        mTryDemoButton = findViewById(R.id.try_demo);
        mFacebookLoginButton = findViewById(R.id.facebook_login);

        mFacebookLoginButton.setOnClickListener(this::onFacebookLogin);
        mTryDemoButton.setOnClickListener(this::onTryDemo);
        findViewById(R.id.try_demo_invite).setOnClickListener(this::onTryDemo);
        setState(State.INIT);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mUser.getPrivateKey() != null) {
            if (mUser.isDemoUser()) {
                getDemoTeams(mUser.getPrivateKey());
            } else {
                getTeams(mUser.getPrivateKey());
            }
        }
    }

    private void setState(State state) {
        switch (state) {
            case INIT:
                mFacebookLoginButton.setVisibility(View.VISIBLE);
                mTryDemoButton.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.GONE);
                break;
            case LOADING:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.GONE);
                break;
            case INVITE_ONLY:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.VISIBLE);
                break;
        }

        mState = state;
    }

    private void getTeams(String privateKey) {
        setState(State.LOADING);
        request(TeambrellaUris.getMyTeams(), privateKey);
    }

    private void getDemoTeams(String privateKey) {
        setState(State.LOADING);
        request(TeambrellaUris.getDemoTeams(Locale.getDefault().getLanguage()), privateKey);
    }


    private void onFacebookLogin(View v) {
        setState(State.LOADING);
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                registerUser(token.getToken(), mUser.getPendingPrivateKey());
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onCancel() {
                setState(State.INVITE_ONLY);
            }

            @Override
            public void onError(FacebookException error) {
                Answers.getInstance().logLogin(new LoginEvent().putSuccess(false));
                tryAgainLater(error);
            }
        });

        LinkedList<String> permissions = new LinkedList<>();
        permissions.add("public_profile");
        permissions.add("email");
        loginManager.logInWithReadPermissions(this, permissions);
    }

    @Override
    public void onBackPressed() {
        switch (mState) {
            case INVITE_ONLY:
                setState(State.INIT);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    private void registerUser(String token, final String privateKey) {
        findViewById(R.id.facebook_login).setVisibility(View.GONE);
        findViewById(R.id.try_demo).setVisibility(View.GONE);
        String publicKeySignature = null;
        try {
            publicKeySignature = EtherAccount.toPublicKeySignature(privateKey, getApplicationContext());
        } catch (CryptoException e) {
            Log.e(LOG_TAG, "Was unnable to generate eth address from the private key. Only public key will be registered on the server. The error was: " + e.getMessage(), e);
        }
        request(TeambrellaUris.getRegisterUri(token, publicKeySignature), privateKey);
    }


    private void tryAgainLater(Throwable throwable) {
        Snackbar.make(findViewById(R.id.facebook_login), R.string.unable_to_connect_try_later, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        setState(State.INIT);
                    }
                })
                .show();
        Crashlytics.logException(throwable);
    }


    private void onTryDemo(View v) {
        mUser.setDemoUser();
        getDemoTeams(mUser.getPrivateKey());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onRequestResult(Notification<JsonObject> response) {
        super.onRequestResult(response);
        if (response.isOnNext()) {
            JsonWrapper result = new JsonWrapper(response.getValue());
            JsonWrapper status = result.getObject(TeambrellaModel.ATTR_STATUS);
            String uriString = status != null ? status.getString(TeambrellaModel.ATTR_STATUS_URI) : null;
            Uri uri = uriString != null ? Uri.parse(uriString) : null;
            if (uri != null) {
                switch (TeambrellaUris.sUriMatcher.match(Uri.parse(uriString))) {
                    case TeambrellaUris.MY_TEAMS:
                    case TeambrellaUris.DEMO_TEAMS: {
                        final int selectedTeam = TeambrellaUser.get(this).getTeamId();
                        JsonWrapper data = result.getObject(TeambrellaModel.ATTR_DATA);
                        String userId = data != null ? data.getString(TeambrellaModel.ATTR_DATA_USER_ID) : null;
                        if (userId != null) {
                            mUser.setUserId(userId);
                        }

                        ArrayList<JsonWrapper> teams = data != null ? data.getArray(TeambrellaModel.ATTR_DATA_MY_TEAMS) : new ArrayList<>();
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
                                , mUser.getUserId()
                                , team.getObject().toString()));

                        mUser.setTeamId(team.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
                        finish();
                    }
                    break;
                    case TeambrellaUris.ME_REGISTER_KEY:
                        mUser.setPrivateKey(mUser.getPendingPrivateKey());
                        Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
                        getTeams(mUser.getPrivateKey());
                        break;
                }
            }
        } else {
            tryAgainLater(response.getError());
        }
    }
}
