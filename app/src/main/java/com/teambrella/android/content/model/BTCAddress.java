package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

public class BTCAddress {

    @SerializedName(TeambrellaModel.ATTR_DATA_ADDRESS)
    public String address;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public String teamateId;

    @SerializedName(TeambrellaModel.ATTR_DATA_STATUS)
    public int status;

    @SerializedName(TeambrellaModel.ATTR_DATA_DATE_CREATED)
    public String dateCreated;
}
