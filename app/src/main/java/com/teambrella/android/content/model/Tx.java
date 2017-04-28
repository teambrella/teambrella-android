package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Tx
 */
public class Tx {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_BTC_AMOUNT)
    public float btcAmount;

    @SerializedName(TeambrellaModel.ATTR_DATA_CLAIM_ID)
    public long claimId;

    @SerializedName(TeambrellaModel.ATTR_DATA_CLAIM_TEAMMATE_ID)
    public long claimTeammateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_WITHDRAW_REQ_ID)
    public long withdrawReqId;

    @SerializedName(TeambrellaModel.ATTR_DATA_KIND)
    public int kind;

    @SerializedName(TeambrellaModel.ATTR_DATA_STATE)
    public int state;

    @SerializedName(TeambrellaModel.ATTR_DATA_INITIATED_TIME)
    public String initiatedTime;
}
