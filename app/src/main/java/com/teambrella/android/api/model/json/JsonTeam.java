package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITeam;

/**
 * Json Team
 */
public class JsonTeam extends JsonWrapper implements ITeam {

    JsonTeam(JsonObject object) {
        super(object);
    }

    @Override
    public int getId() {
        return getInt(TeambrellaModel.ATTR_DATA_ID, -1);
    }

    @Override
    public String getName() {
        return getString(TeambrellaModel.ATTR_DATA_NAME);
    }

    @Override
    public boolean isTestNet() {
        return getBoolean(TeambrellaModel.ATTR_DATA_TEST_NET, false);
    }
}
