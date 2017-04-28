package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Cosigner
 */
public class Cosigner {

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS_ID)
    public String addressId;

    @SerializedName(TeambrellaModel.ATTR_DATA_KEY_ORDER)
    public int keyOrder;
}
