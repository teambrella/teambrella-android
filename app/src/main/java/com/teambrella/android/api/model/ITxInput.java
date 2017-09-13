package com.teambrella.android.api.model;

/**
 * Transaction input
 */
public interface ITxInput {

    String getId();

    String getTxId();

    float getCryptoAmount();

    String getPreviousTxId();

    int getPreviousTxIndex();
}
