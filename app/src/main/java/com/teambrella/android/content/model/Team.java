package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

/**
 * Team
 */
public class Team {

    @Column(TeambrellaRepository.Team.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public long id;

    @Column(TeambrellaRepository.Team.NAME)
    @SerializedName(TeambrellaModel.ATTR_DATA_NAME)
    public String name;

    @Column(TeambrellaRepository.Team.TESTNET)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEST_NET)
    public boolean testNet;

}
