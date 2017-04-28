package com.teambrella.android.content.model;


import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * PayTo
 */
public class PayTo {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public String id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_KNOWN_SINCE)
    public String knownSince;

    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS)
    public String address;

    @SerializedName(TeambrellaModel.ATTR_DATA_IS_DEFAULT)
    public boolean isDefault;

}
