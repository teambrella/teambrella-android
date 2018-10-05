package com.teambrella.android.backup;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.teambrella.android.util.log.Log;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Wallet backup manager
 */
public class WalletBackupManager {

    private static final String LOG_TAG = WalletBackupManager.class.getSimpleName();

    private static final String VKONTAKTE = "vkontakte";
    private static final String NO_SOCIAL = "no|";

    private static final int SAVE_WALLET_REQUEST_CODE = 205;
    private static final int READ_WALLET_REQUEST_CODE = 206;


    public interface IWalletBackupListener {

        int FAILED = 1;
        int RESOLUTION_REQUIRED = 2;
        int CANCELED = 3;


        void onWalletSaved(boolean force);

        void onWalletSaveError(int code, boolean force);

        void onWalletRead(String key, boolean force);

        void onWalletReadError(int code, boolean force);

    }


    /**
     * API Client
     */
    private final GoogleApiClient mGoogleApiClient;


    private final CopyOnWriteArrayList<IWalletBackupListener> mListeners = new CopyOnWriteArrayList<>();

    private FragmentActivity mActivity;

    private Boolean mForceReadOnConnected = null;

    /**
     * Constructor
     *
     * @param activity to use
     */
    public WalletBackupManager(FragmentActivity activity) {
        mActivity = activity;

        CredentialsOptions options =
                new CredentialsOptions.Builder().forceEnableSaveDialog().build();
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(mConnectionCallbacks)
                .enableAutoManage(activity, mConnectionFailedListener)
                .addApi(Auth.CREDENTIALS_API, options)
                .build();
    }

    public void setActivity(FragmentActivity activity) {
        mActivity = activity;
    }

    public void addBackupListener(IWalletBackupListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeBackupListener(IWalletBackupListener listener) {
        mListeners.remove(listener);
    }


    public void readOnConnected(@SuppressWarnings("SameParameterValue") boolean force) {
        mForceReadOnConnected = force;
    }

    public void saveWallet(String id, String name, Uri picture, String password, final boolean force) {

        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        String login = id.startsWith(VKONTAKTE) ? String.format("vk.com/id%s", id.substring(10, id.length()))
                : id.startsWith(NO_SOCIAL) ? id.substring(3, id.length()) : String.format("fb.com/%s", id);


        Credential credential = new Credential.Builder(login)
                .setName(name)
                .setPassword(password)
                .setProfilePictureUri(picture)
                .build();

        Auth.CredentialsApi.save(mGoogleApiClient, credential).setResultCallback(
                result -> {
                    Status status = result.getStatus();
                    if (status.isSuccess()) {
                        notifyOnWalletSaved(force);
                    } else {
                        if (status.hasResolution()) {
                            if (force) {
                                try {
                                    status.startResolutionForResult(mActivity, SAVE_WALLET_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    notifyOnWalletSaveError(IWalletBackupListener.FAILED, true);
                                }
                            } else {
                                notifyOnWalletSaveError(IWalletBackupListener.RESOLUTION_REQUIRED, false);
                            }
                        } else {
                            Log.reportNonFatal(LOG_TAG, new RuntimeException("unable to write wallet " + status));
                            notifyOnWalletSaveError(status.isCanceled() ? IWalletBackupListener.CANCELED : IWalletBackupListener.FAILED, force);
                        }
                    }
                });
    }

    public void readWallet(boolean force) {

        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(credentialRequestResult -> {
            if (credentialRequestResult.getStatus().isSuccess()) {
                Credential credential = credentialRequestResult.getCredential();
                notifyOnWalletRead(credential.getPassword(), force);
            } else {
                Status status = credentialRequestResult.getStatus();
                if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    if (force) {
                        try {
                            credentialRequestResult.getStatus().startResolutionForResult(mActivity, READ_WALLET_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            notifyOnWalletReadError(IWalletBackupListener.FAILED, true);
                        }
                    } else {
                        notifyOnWalletReadError(IWalletBackupListener.RESOLUTION_REQUIRED, false);
                    }
                } else {
                    Log.reportNonFatal(LOG_TAG, new RuntimeException("unable to read wallet " + status));
                    notifyOnWalletReadError(status.isCanceled() ? IWalletBackupListener.CANCELED : IWalletBackupListener.FAILED, force);
                }
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_WALLET_REQUEST_CODE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                notifyOnWalletSaved(true);
            } else {
                notifyOnWalletSaveError(IWalletBackupListener.CANCELED, true);
            }
        } else if (requestCode == READ_WALLET_REQUEST_CODE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                notifyOnWalletRead(credential.getPassword(), true);
            } else {
                notifyOnWalletReadError(IWalletBackupListener.CANCELED, true);
            }
        }
    }


    private void notifyOnWalletSaved(boolean force) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletSaved(force);
        }
    }

    private void notifyOnWalletSaveError(int code, boolean force) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletSaveError(code, force);
        }
    }

    private void notifyOnWalletRead(String key, boolean force) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletRead(key, force);
        }
    }

    private void notifyOnWalletReadError(int code, boolean force) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletReadError(code, force);
        }
    }


    @SuppressWarnings("FieldCanBeLocal")
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mForceReadOnConnected != null) {
                readWallet(mForceReadOnConnected);
                mForceReadOnConnected = null;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };


    @SuppressWarnings("FieldCanBeLocal")
    private final GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener =
            connectionResult -> Log.reportNonFatal(LOG_TAG, new RuntimeException(connectionResult.getErrorMessage()));
}
