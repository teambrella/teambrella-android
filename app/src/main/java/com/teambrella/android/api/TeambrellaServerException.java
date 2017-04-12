package com.teambrella.android.api;

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
    public TeambrellaServerException(int errorCode, String errorMessage, long timestamp) {
        super();
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
