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

    @Column(TeambrellaRepository.Cosigner.ADDRESS_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS_ID)
    public String addressId;

    @Column(TeambrellaRepository.Cosigner.KEY_ORDER)
    @SerializedName(TeambrellaModel.ATTR_DATA_KEY_ORDER)
    public int keyOrder;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY)
    public String publicKey;


    @Override
    public int compareTo(@NonNull Cosigner o) {
        return Integer.valueOf(keyOrder).compareTo(o.keyOrder);
    }
}