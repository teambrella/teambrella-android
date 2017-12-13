package com.teambrella.android.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaClientException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.backup.TeambrellaBackupData;
import com.teambrella.android.blockchain.CryptoException;
import com.teambrella.android.blockchain.EtherAccount;
import com.teambrella.android.ui.base.AppCompatRequestActivity;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.StatisticHelper;
import com.teambrella.android.util.log.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import io.reactivex.Notification;

/**
 * Wellcome activity
 */
public class WelcomeActivity extends AppCompatRequestActivity {

    public static final String FACEBOOK_ID_EXTRA = "EXTRA_FACEBOOK_ID";

    public static final String CUSTOM_ACTION = "extra_custom_action";

    private static final int READ_PRIVATE_KEY_REQUEST_CODE = 105;

    private enum State {
        INIT,
        LOADING,
        INVITE_ONLY,
        ALMOST_READY,
        ACCESS_DENIED,
        PENDING_APPLICATION
    }


    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();
    private CallbackManager mCallBackManager = CallbackManager.Factory.create();
    private View mInvitationOnlyView;
    private View mFacebookLoginButton;
    private View mTryDemoButton;
    private View mTryDemoInvite;
    private View mMarginView;
    private TextView mInvitationTitle;
    private TextView mInvitationDescription;
    private TeambrellaUser mUser;
    private State mState;
    private String mFacebookId;
    private TeambrellaBackupData mBackupData;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = TeambrellaUser.get(this);
        mBackupData = new TeambrellaBackupData(this);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.acivity_welcome);

        mInvitationOnlyView = findViewById(R.id.invitation_only);
        mTryDemoButton = findViewById(R.id.try_demo);
        mFacebookLoginButton = findViewById(R.id.facebook_login);
        mInvitationDescription = findViewById(R.id.invitation_description);
        mInvitationTitle = findViewById(R.id.invitation_title);
        mFacebookLoginButton.setOnClickListener(this::onFacebookLogin);
        mTryDemoButton.setOnClickListener(this::onTryDemo);
        mTryDemoInvite = findViewById(R.id.try_demo_invite);
        mMarginView = findViewById(R.id.margin);
        mTryDemoInvite.setOnClickListener(this::onTryDemo);

        mFacebookId = savedInstanceState != null ? savedInstanceState.getString(FACEBOOK_ID_EXTRA, null) : null;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mConnectionCallbacks)
                .enableAutoManage(this, mConnectionFailedListener)
                .addApi(Auth.CREDENTIALS_API)
                .build();

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
        } else if (BuildConfig.DEBUG) {
            Intent intent = getIntent();
            Uri uri = intent.getData();
            if (uri != null) {
                mUser.setPrivateKey(uri.getQueryParameter("key"));
                getTeams(mUser.getPrivateKey());
            }
        }
    }

    private void setState(State state) {
        switch (state) {
            case INIT:
                mFacebookLoginButton.setVisibility(View.VISIBLE);
                mTryDemoButton.setVisibility(View.VISIBLE);
                mInvitationOnlyView.setVisibility(View.GONE);
                mTryDemoInvite.setVisibility(View.VISIBLE);
                mMarginView.setVisibility(View.GONE);
                break;
            case LOADING:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.GONE);
                mMarginView.setVisibility(View.GONE);
                break;
            case INVITE_ONLY:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.VISIBLE);
                mInvitationTitle.setText(R.string.we_are_invite_only_title);
                setInvitationDescription(R.string.we_are_invite_only_description);
                mMarginView.setVisibility(View.GONE);
                break;
            case ALMOST_READY:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.VISIBLE);
                mInvitationTitle.setText(R.string.almost_ready_title);
                mInvitationDescription.setText(R.string.almost_ready_description);
                mMarginView.setVisibility(View.GONE);
                break;
            case ACCESS_DENIED:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.VISIBLE);
                mInvitationTitle.setText(R.string.access_denied_title);
                setInvitationDescription(R.string.access_denied_description);
                mTryDemoInvite.setVisibility(View.GONE);
                mMarginView.setVisibility(View.VISIBLE);
                break;
            case PENDING_APPLICATION:
                mFacebookLoginButton.setVisibility(View.INVISIBLE);
                mTryDemoButton.setVisibility(View.INVISIBLE);
                mInvitationOnlyView.setVisibility(View.VISIBLE);
                mInvitationTitle.setText(R.string.pending_application_title);
                setInvitationDescription(R.string.pending_application_description);
                mTryDemoInvite.setVisibility(View.GONE);
                mMarginView.setVisibility(View.VISIBLE);
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
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(credentialRequestResult -> {
            if (credentialRequestResult.getStatus().isSuccess()) {
                Credential credential = credentialRequestResult.getCredential();
                mUser.setPrivateKey(credential.getPassword());
                getTeams(mUser.getPrivateKey());
            } else {
                if (credentialRequestResult.getStatus().getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        credentialRequestResult.getStatus().startResolutionForResult(this, READ_PRIVATE_KEY_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        loginByFacebook();
                    }
                } else {
                    loginByFacebook();
                }
            }
        });
    }


    private void loginByFacebook() {
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                mFacebookId = loginResult.getAccessToken().getUserId();

                String backUpKey = mBackupData.getValue(Integer.toString(mFacebookId.hashCode()));
                if (backUpKey == null) {
                    backUpKey = mUser.getPendingPrivateKey();
                }
                ECKey key = DumpedPrivateKey.fromBase58(null, backUpKey).getKey();
                registerUser(token.getToken(), backUpKey, key.getPublicKeyAsHex());
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onCancel() {
                setState(State.INVITE_ONLY);
            }

            @Override
            public void onError(FacebookException error) {
                if (!BuildConfig.DEBUG) {
                    Answers.getInstance().logLogin(new LoginEvent().putSuccess(false));
                }
                tryAgainLater(error);
            }
        });

        LinkedList<String> permissions = new LinkedList<>();
        permissions.add("public_profile");
        permissions.add("email");
        loginManager.logInWithReadPermissions(this, permissions);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FACEBOOK_ID_EXTRA, mFacebookId);
    }

    @Override
    public void onBackPressed() {
        switch (mState) {
            case INVITE_ONLY:
            case ALMOST_READY:
            case ACCESS_DENIED:
            case PENDING_APPLICATION:
                setState(State.INIT);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    private void registerUser(String token, final String privateKey, String publicKeyHex) {
        findViewById(R.id.facebook_login).setVisibility(View.GONE);
        findViewById(R.id.try_demo).setVisibility(View.GONE);
        String publicKeySignature = null;
        try {
            publicKeySignature = EtherAccount.toPublicKeySignature(privateKey, getApplicationContext(), publicKeyHex);
        } catch (CryptoException e) {
            Log.e(LOG_TAG, "Was unnable to generate eth address from the private key. Only public key will be registered on the server. The error was: " + e.getMessage(), e);
        }
        request(TeambrellaUris.getRegisterUri(token, publicKeySignature), privateKey);
    }


    private void tryAgainLater(Throwable throwable) {
        boolean isInternetAvailable = ConnectivityUtils.isNetworkAvailable(this);
        Snackbar.make(findViewById(R.id.facebook_login), isInternetAvailable ? R.string.unable_to_connect_try_later : R.string.no_internet_connection, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (mUser.getPrivateKey() != null
                                && !mUser.isDemoUser()) {
                            finish();
                        } else {
                            setState(State.INIT);
                        }
                    }
                })
                .show();
        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(throwable);
        }
    }

    private void noInternetConnectionTryAgain(final Uri uri, Throwable throwable) {
        Snackbar.make(findViewById(R.id.facebook_login), R.string.no_internet_connection, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, view -> {
                    switch (TeambrellaUris.sUriMatcher.match(uri)) {
                        case TeambrellaUris.DEMO_TEAMS:
                            onTryDemo(findViewById(R.id.try_demo));
                        default:
                            request(uri);
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.lightGold))
                .show();
        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(throwable);
        }
    }


    private void onTryDemo(View v) {
        mUser.setDemoUser();
        getDemoTeams(mUser.getPrivateKey());
        StatisticHelper.onTryDemo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_PRIVATE_KEY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                mUser.setPrivateKey(credential.getPassword());
                getTeams(mUser.getPrivateKey());
            } else {
                loginByFacebook();
            }
        }
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
                            StatisticHelper.setUserId(userId);
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
                            setState(State.INVITE_ONLY);
                            return;
                        }

                        startActivity(MainActivity.getLaunchIntent(WelcomeActivity.this
                                , mUser.getUserId()
                                , team.getObject().toString()).setAction(getIntent().getStringExtra(CUSTOM_ACTION)));

                        mUser.setTeamId(team.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
                        finish();
                    }
                    break;
                    case TeambrellaUris.ME_REGISTER_KEY:

                        String privateKey = mFacebookId != null ? mBackupData.getValue(Integer.toString(mFacebookId.hashCode())) : null;

                        if (privateKey == null) {
                            privateKey = mUser.getPendingPrivateKey();
                        }

                        mUser.setPrivateKey(privateKey);

                        if (mFacebookId != null) {
                            mBackupData.setValue(Integer.toString(mFacebookId.hashCode()), privateKey);
                            mFacebookId = null;
                        }

                        StatisticHelper.onUserRegistered();
                        if (!BuildConfig.DEBUG) {
                            Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
                        }
                        getTeams(mUser.getPrivateKey());
                        break;
                }
            }
        } else {
            @SuppressWarnings("ThrowableNotThrown")
            Throwable error = response.getError();
            if (error instanceof TeambrellaServerException) {
                TeambrellaServerException exception = (TeambrellaServerException) error;
                switch (exception.getErrorCode()) {
                    case TeambrellaModel.VALUE_STATUS_RESULT_USER_HAS_NO_TEAM:
                        setState(State.INVITE_ONLY);
                        break;
                    case TeambrellaModel.VALUE_STATUS_RESULT_USER_HAS_NO_TEAM_BUT_APPLICTION_APPROVED:
                        setState(State.ALMOST_READY);
                        break;
                    case TeambrellaModel.VALUE_STATUS_RESULT_USER_HAS_ANOTHER_KEY:
                        setState(State.ACCESS_DENIED);
                        if (mFacebookId != null) {
                            mBackupData.setValue(Integer.toString(mFacebookId.hashCode()), null);
                        }
                        break;
                    case TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH:
                        setState(State.INIT);
                        break;
                    case TeambrellaModel.VALUE_STATUS_RESULT_USER_HAS_NO_TEAM_BUT_APPLICTION_PENDING:
                        setState(State.PENDING_APPLICATION);
                        break;
                    default:
                        tryAgainLater(error);
                        break;
                }
                mFacebookId = null;
            } else if (error instanceof TeambrellaClientException) {
                Throwable cause = error.getCause();
                if (cause instanceof SocketTimeoutException
                        || cause instanceof UnknownHostException) {
                    noInternetConnectionTryAgain(((TeambrellaClientException) error).getUri(), error);
                } else {
                    mFacebookId = null;
                    tryAgainLater(error);
                }
            } else {
                tryAgainLater(error);
                mFacebookId = null;
            }
        }
    }

    private void setInvitationDescription(@StringRes int textId) {
        Spannable text = (Spannable) Html.fromHtml(getString(textId));
        mInvitationDescription.setMovementMethod(LinkMovementMethod.getInstance());
        mInvitationDescription.setText(text);
    }


    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };


    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = connectionResult -> {

    };
}
