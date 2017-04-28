package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Team
 */
public class Team {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public long id;

    @SerializedName(TeambrellaModel.ATTR_DATA_NAME)
    public String name;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEST_NET)
    public boolean testNet;

}
