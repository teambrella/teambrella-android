package com.teambrella.android.content.model;


import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

/**
 * PayTo
 */
public class PayTo {

    @Column(TeambrellaRepository.PayTo.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @Column(TeambrellaRepository.PayTo.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @Column(TeambrellaRepository.PayTo.KNOWN_SINCE)
    @SerializedName(TeambrellaModel.ATTR_DATA_KNOWN_SINCE)
    public String knownSince;

    @Column(TeambrellaRepository.PayTo.ADDRESS)
    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS)
    public String address;

    @Column(TeambrellaRepository.PayTo.IS_DEFAULT)
    @SerializedName(TeambrellaModel.ATTR_DATA_IS_DEFAULT)
    public boolean isDefault;

}
