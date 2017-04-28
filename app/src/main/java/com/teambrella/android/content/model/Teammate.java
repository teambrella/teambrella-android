package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;


public class Teammate {

    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public long id;

    @SerializedName(TeambrellaModel.ATTR_DATA_TEAM_ID)
    public long teamId;

    @SerializedName(TeambrellaModel.ATTR_DATA_NAME)
    public String name;

    @SerializedName(TeambrellaModel.ATTR_DATA_FB_NAME)
    public String facebookName;

    @SerializedName(TeambrellaModel.ATTR_DATA_PUBLIC_KEY)
    public String publicKey;
}
