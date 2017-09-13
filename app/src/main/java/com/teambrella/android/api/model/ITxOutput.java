package com.teambrella.android.api.model;

/**
 * Transaction output
 */
public interface ITxOutput {

    String getId();

    String getTxId();

    String getPayToId();

    float getCryptoAmount();
}
