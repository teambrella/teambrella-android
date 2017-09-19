package com.teambrella.android.api;

import android.net.Uri;

/**
 * Teambrella Client Exception
 */
public class TeambrellaClientException extends TeambrellaException {
    public TeambrellaClientException(Uri uri, String message, Throwable cause) {
        super(uri, message, cause);
    }

    @Override
    public String toString() {
        return "[" + getUri() + "] " + super.toString();
    }
}
