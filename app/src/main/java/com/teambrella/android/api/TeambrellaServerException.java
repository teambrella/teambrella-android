package com.teambrella.android.api;

import android.net.Uri;

/**
 * Teambrella server exception
 */
public class TeambrellaServerException extends TeambrellaException {

    /**
     * Error Code
     */
    private final int mErrorCode;


    /**
     * Error message
     */
    private final String mErrorMessage;


    /**
     * Timestamp
     */
    private final long mTimestamp;


    /**
     * Constructor.
     *
     * @param errorCode    server result code
     * @param errorMessage server error message
     */
    public TeambrellaServerException(Uri uri, int errorCode, String errorMessage, long timestamp) {
        super(uri);
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
        this.mTimestamp = timestamp;
    }


    public int getErrorCode() {
        return mErrorCode;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
