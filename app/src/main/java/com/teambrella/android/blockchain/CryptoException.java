package com.teambrella.android.blockchain;

import android.net.Uri;

/**
 * Cryptographic exception that occurred locally in a mobile lib.
 */
public class CryptoException extends Exception {

    public CryptoException(String message) {
        super(message);
    }

    CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}

