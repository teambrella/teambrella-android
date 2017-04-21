package com.teambrella.android.api.model;

public interface ITx {

    String getId();

    long getTeammateId();

    float getBTCAmount();

    long getClaimId();

    long getWithdrawReqId();

    long getClaimTeammateId();

    int getKind();

    int getState();

    String getInitiatedTime();


}
