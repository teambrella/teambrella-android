package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.Map;
import java.util.UUID;

/**
 * Tx Input
 */
public class TxInput {

    @Column(TeambrellaRepository.TXInput.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public UUID id;
    @Column(TeambrellaRepository.TXInput.TX_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public UUID txId;
    @Column(TeambrellaRepository.TXInput.AMMOUNT_BTC)
    @SerializedName(TeambrellaModel.ATTR_DATA_BTC_AMOUNT)
    public String btcAmount;
    @Column(TeambrellaRepository.TXInput.PREV_TX_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_ID)
    public String previousTxId;
    @Column(TeambrellaRepository.TXInput.PREV_TX_INDEX)
    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_INDEX)
    public int previousTxIndex;

    /**
     * Signatures
     */
    public Map<Long, TXSignature> signatures;

}
