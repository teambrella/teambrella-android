package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.List;

public class Multisig {

    @Column(TeambrellaRepository.Multisig.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public long id;

    @Column(TeambrellaRepository.Multisig.ADDRESS)
    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS)
    public String address;

    @Column(TeambrellaRepository.Multisig.CREATION_TX)
    @SerializedName(TeambrellaModel.ATTR_DATA_CREATION_TX)
    public String creationTx;

    @Column(TeambrellaRepository.Multisig.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @Column(TeambrellaRepository.Multisig.STATUS)
    @SerializedName(TeambrellaModel.ATTR_DATA_STATUS)
    public int status;

    @Column(TeambrellaRepository.Multisig.DATE_CREATED)
    @SerializedName(TeambrellaModel.ATTR_DATA_DATE_CREATED)
    public String dateCreated;

    @Column(TeambrellaRepository.Teammate.NAME)
    public String teammateName;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY)
    public String teammatePublicKey;

    @Column(TeambrellaRepository.Teammate.TEAM_ID)
    public long teamId;


    public List<Cosigner> cosigners;
    public Unconfirmed unconfirmed;

}
