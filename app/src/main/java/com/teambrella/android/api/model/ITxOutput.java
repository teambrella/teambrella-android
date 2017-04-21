package com.teambrella.android.api.model;

/**
 * Transaction output
 */
public interface ITxOutput {

    long getId();

    String getTxId();

    String getPayToId();

    float getBTCAmount();
}
