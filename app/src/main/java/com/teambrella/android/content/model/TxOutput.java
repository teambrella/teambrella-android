package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.UUID;

/**
 * Tx Output
 */
public class TxOutput {

    @Column(TeambrellaRepository.TXOutput.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public UUID id;
    @Column(TeambrellaRepository.TXOutput.TX_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public UUID txId;
    @Column(TeambrellaRepository.TXOutput.PAY_TO_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_PAY_TO_ID)
    public String payToId;
    @Column(TeambrellaRepository.TXOutput.AMOUNT_BTC)
    @SerializedName(TeambrellaModel.ATTR_DATA_BTC_AMOUNT)
    public String btcAmount;

    @Column(TeambrellaRepository.PayTo.TEAMMATE_ID)
    public long teammateId;

    @Column(TeambrellaRepository.PayTo.KNOWN_SINCE)
    public String knownSince;

    @Column(TeambrellaRepository.PayTo.ADDRESS)
    public String address;

    @Column(TeambrellaRepository.PayTo.IS_DEFAULT)
    public boolean isDefault;
}