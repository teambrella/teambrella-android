package com.teambrella.android.api.model;

/**
 * BTC Address
 */
public interface IBTCAddress {

    int USER_ADDRESS_STATUS_PREVIOUS = 0;
    int USER_ADDRESS_STATUS_CURRENT = 1;
    int USER_ADDRESS_STATUS_NEXT = 2;
    int USER_ADDRESS_STATUS_ARCHIVE = 3;
    int USER_ADDRESS_STATUS_INVALID = 4;
    int USER_ADDRESS_STATUS_SERVER_PREVIOUS = 10;
    int USER_ADDRESS_STATUS_SERVER_CURRENT = 11;
    int USER_ADDRESS_STATUS_SERVER_NEXT = 12;

    String getAddress();

    String getTeammateId();

    int getStatus();

    String getCreatedDate();
}
