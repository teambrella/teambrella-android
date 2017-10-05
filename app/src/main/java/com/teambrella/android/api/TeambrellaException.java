package com.teambrella.android.api;

import android.net.Uri;

/**
 * Teambrella exception
 */
public class TeambrellaException extends Exception {

    protected final Uri mUri;

    public TeambrellaException(Uri uri) {
        mUri = uri;
    }

    TeambrellaException(Uri uri, String message, Throwable cause) {
        super(message, cause);
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }
}

