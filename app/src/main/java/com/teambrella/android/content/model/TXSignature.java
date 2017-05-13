package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

/**
 * Tx Signature
 */
public class TXSignature {
    @Column(TeambrellaRepository.TXSignature.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;
    @Column(TeambrellaRepository.TXSignature.TX_INPUT_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TX_INPUT_ID)
    public String txInputId;
    @Column(TeambrellaRepository.TXSignature.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;
    @Column(TeambrellaRepository.TXSignature.SIGNATURE)
    @SerializedName(TeambrellaModel.ATTR_DATA_SIGNATURE)
    public String signature;
}
