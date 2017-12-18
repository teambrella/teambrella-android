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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Wallet backup manager
 */
public class WalletBackupManager {


    private static final int SAVE_WALLET_REQUEST_CODE = 205;
    private static final int READ_WALLET_REQUEST_CODE = 206;


    public interface IWalletBackupListener {

        int FAILED = 1;
        int RESOLUTION_REQUIRED = 2;


        void onWalletSaved();

        void onWalletSaveError(int code);

        void onWalletRead(String key);

        void onWalletReadError(int code);

    }


    /**
     * API Client
     */
    private final GoogleApiClient mGoogleApiClient;


    private final CopyOnWriteArrayList<IWalletBackupListener> mListeners = new CopyOnWriteArrayList<>();

    private final FragmentActivity mActivity;

    /**
     * Constructor
     *
     * @param activity to use
     */
    public WalletBackupManager(FragmentActivity activity) {
        mActivity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(mConnectionCallbacks)
                .enableAutoManage(activity, mConnectionFailedListener)
                .addApi(Auth.CREDENTIALS_API)
                .build();
    }

    public void addBackupListener(IWalletBackupListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeBackupListener(IWalletBackupListener listener) {
        mListeners.remove(listener);
    }

    public void saveWallet(String id, String name, Uri picture, String password, final boolean force) {
        Credential credential = new Credential.Builder(id)
                .setName(name)
                .setPassword(password)
                .setProfilePictureUri(picture)
                .build();

        Auth.CredentialsApi.save(mGoogleApiClient, credential).setResultCallback(
                result -> {
                    Status status = result.getStatus();
                    if (status.isSuccess()) {
                        notifyOnWalletSaved();
                    } else {
                        if (status.hasResolution()) {
                            if (force) {
                                try {
                                    status.startResolutionForResult(mActivity, SAVE_WALLET_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    notifyOnWalletSaveError(IWalletBackupListener.FAILED);
                                }
                            } else {
                                notifyOnWalletSaveError(IWalletBackupListener.RESOLUTION_REQUIRED);
                            }
                        } else {
                            notifyOnWalletSaveError(IWalletBackupListener.FAILED);
                        }
                    }
                });
    }

    public void readWallet(boolean force) {

        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(credentialRequestResult -> {
            if (credentialRequestResult.getStatus().isSuccess()) {
                Credential credential = credentialRequestResult.getCredential();
                notifyOnWalletRead(credential.getPassword());
            } else {
                if (credentialRequestResult.getStatus().getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    if (force) {
                        try {
                            credentialRequestResult.getStatus().startResolutionForResult(mActivity, READ_WALLET_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            notifyOnWalletReadError(IWalletBackupListener.FAILED);
                        }
                    } else {
                        notifyOnWalletReadError(IWalletBackupListener.RESOLUTION_REQUIRED);
                    }
                } else {
                    notifyOnWalletReadError(IWalletBackupListener.FAILED);
                }
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_WALLET_REQUEST_CODE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                notifyOnWalletSaved();
            } else {
                notifyOnWalletSaveError(IWalletBackupListener.FAILED);
            }
        } else if (requestCode == READ_WALLET_REQUEST_CODE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                notifyOnWalletRead(credential.getPassword());
            } else {
                notifyOnWalletReadError(IWalletBackupListener.FAILED);
            }
        }
    }


    private void notifyOnWalletSaved() {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletSaved();
        }
    }

    private void notifyOnWalletSaveError(int code) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletSaveError(code);
        }
    }

    private void notifyOnWalletRead(String key) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletRead(key);
        }
    }

    private void notifyOnWalletReadError(int code) {
        for (IWalletBackupListener listener : mListeners) {
            listener.onWalletReadError(code);
        }
    }


    @SuppressWarnings("FieldCanBeLocal")
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };


    @SuppressWarnings("FieldCanBeLocal")
    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = connectionResult -> {

    };
}
