package com.teambrella.android.api.model;

/**
 * Pay To
 */
public interface IPayTo {

    String getId();

    long getTeamId();

    String getKnownSince();

    String getAddress();

    boolean isDefault();

}
