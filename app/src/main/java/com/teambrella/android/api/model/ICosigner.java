package com.teambrella.android.api.model;

/**
 * Teambrella Cosigner
 */
public interface ICosigner {

    long getTeammateId();

    long getMultisigId();

    int getKeyOrder();
}
