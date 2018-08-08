package com.teambrella.android.content.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tx Input
 */
public class TxInput implements Comparable<TxInput> {

    @Column(TeambrellaRepository.TXInput.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public UUID id;
    @Column(TeambrellaRepository.TXInput.TX_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public UUID txId;
    @Column(TeambrellaRepository.TXInput.AMOUNT_CRYPTO)
    @SerializedName(TeambrellaModel.ATTR_DATA_CRYPTO_AMOUNT)
    public String cryptoAmount;
    @Column(TeambrellaRepository.TXInput.PREV_TX_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_ID)
    public String previousTxId;
    @Column(TeambrellaRepository.TXInput.PREV_TX_INDEX)
    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_INDEX)
    public int previousTxIndex;

    /**
     * Signatures
     */
    public Map<Long, TXSignature> signatures = new HashMap<>();


    @Override
    public int compareTo(@NonNull TxInput o) {
        return id.toString().toLowerCase()
                .compareTo(o.id.toString().toLowerCase());
    }
}
