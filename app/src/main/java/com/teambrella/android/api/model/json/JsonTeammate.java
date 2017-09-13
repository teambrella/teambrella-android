package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITeammate;

/**
 * Json Teammate
 */
public class JsonTeammate extends JsonWrapper implements ITeammate {

    public JsonTeammate(JsonObject object) {
        super(object);
    }

    @Override
    public long getId() {
        return getLong(TeambrellaModel.ATTR_DATA_ID, -1);
    }

    @Override
    public long getTeamId() {
        return getLong(TeambrellaModel.ATTR_DATA_TEAM_ID, -1);
    }

    @Override
    public String getName() {
        return getString(TeambrellaModel.ATTR_DATA_NAME);
    }

    @Override
    public String getFacebookName() {
        return getString(TeambrellaModel.ATTR_DATA_FB_NAME);
    }

    @Override
    public String getPublicKey() {
        return getString(TeambrellaModel.ATTR_DATA_PUBLIC_KEY);
    }

    @Override
    public String getPublicKeyAddress() { return getString(TeambrellaModel.ATTR_DATA_PUBLIC_KEY_ADDRESS); }
}
