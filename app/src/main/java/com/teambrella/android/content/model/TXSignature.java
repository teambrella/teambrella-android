package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Tx Signature
 */
public class TXSignature {
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TX_ID)
    public String txInputId;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_SIGNATURE)
    public String signature;
}
