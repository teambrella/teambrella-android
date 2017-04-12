package com.teambrella.android.api;

/**
 * Teambrella exception
 */
public class TeambrellaException extends Exception {

    public TeambrellaException() {
    }

    public TeambrellaException(String message) {
        super(message);
    }

    public TeambrellaException(String message, Throwable cause) {
        super(message, cause);
    }
}

