package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Tx Input
 */
public class TxInput {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public String txId;

    @SerializedName(TeambrellaModel.ATTR_DATA_BTC_AMOUNT)
    public float btcAmount;

    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_ID)
    public String previousTxId;

    @SerializedName(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_INDEX)
    public int previousTxIndex;
}
