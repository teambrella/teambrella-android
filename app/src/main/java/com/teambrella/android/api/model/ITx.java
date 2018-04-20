package com.teambrella.android.api.model;

public interface ITx {

    String getId();

    long getTeammateId();

    float getCryptoAmount();

    long getClaimId();

    long getWithdrawReqId();

    long getClaimTeammateId();

    int getKind();

    int getState();

    String getInitiatedTime();

    ITxInput[] getInputs();

    ITxOutput[] getOutputs();
}
