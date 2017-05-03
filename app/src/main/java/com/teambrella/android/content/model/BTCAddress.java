package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

public class BTCAddress {

    @Column(TeambrellaRepository.BTCAddress.ADDRESS)
    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS)
    public String address;

    @Column(TeambrellaRepository.BTCAddress.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public String teammateId;

    @Column(TeambrellaRepository.BTCAddress.STATUS)
    @SerializedName(TeambrellaModel.ATTR_DATA_STATUS)
    public int status;

    @Column(TeambrellaRepository.BTCAddress.DATE_CREATED)
    @SerializedName(TeambrellaModel.ATTR_DATA_DATE_CREATED)
    public String dateCreated;

    @Column(TeambrellaRepository.Teammate.NAME)
    public String teammateName;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY)
    public String teammatePublicKey;

    @Column(TeambrellaRepository.Teammate.TEAM_ID)
    public String teamId;

}
