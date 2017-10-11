package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.Date;
import java.util.List;

public class Unconfirmed {

    @Column(TeambrellaRepository.Unconfirmed.ID)
    public long id;

    @Column(TeambrellaRepository.Unconfirmed.MULTISIG_ID)
    public String address;

    @Column(TeambrellaRepository.Unconfirmed.TX_ID)
    public String creationTx;

    @Column(TeambrellaRepository.Unconfirmed.CRYPTO_TX)
    public long teammateId;

    @Column(TeambrellaRepository.Unconfirmed.CRYPTO_FEE)
    public int status;

    @Column(TeambrellaRepository.Unconfirmed.DATE_CREATED)
    public String dateCreated;
}
