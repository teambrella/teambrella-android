package com.teambrella.android.content.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

/**
 * Cosigner
 */
public class Cosigner implements Comparable<Cosigner> {

    @Column(TeambrellaRepository.Cosigner.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @Column(TeambrellaRepository.Cosigner.MULTISIG_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_MULTISIG_ID)
    public long multisigId;

    @Column(TeambrellaRepository.Cosigner.KEY_ORDER)
    @SerializedName(TeambrellaModel.ATTR_DATA_KEY_ORDER)
    public int keyOrder;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY)
    public String publicKey;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY_ADDRESS)
    public String publicKeyAddress;


    @Override
    public int compareTo(@NonNull Cosigner o) {
        return Integer.valueOf(keyOrder).compareTo(o.keyOrder);
    }
}
