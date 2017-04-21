package com.teambrella.android.api.model;

/**
 * Transaction input
 */
public interface ITxInput {

    long getId();

    long getTxId();

    float getBTCAmount();

    String getPreviousTxId();

    int getPreviosTxIndex();
}
