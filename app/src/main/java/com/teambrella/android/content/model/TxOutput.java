package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Tx Output
 */
public class TxOutput {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public String txId;

    @SerializedName(TeambrellaModel.ATTR_DATA_PAY_TO_ID)
    public String payToId;

    @SerializedName(TeambrellaModel.ATTR_DATA_BTC_AMOUNT)
    public float btcAmount;
}
