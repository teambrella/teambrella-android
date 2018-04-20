package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.IPayTo;

/**
 * Json Pay To
 */
public class JsonPayTo extends JsonWrapper implements IPayTo {


    public JsonPayTo(JsonObject object) {
        super(object);
    }

    @Override
    public String getId() {
        return getString(TeambrellaModel.ATTR_DATA_ID);
    }

    @Override
    public long getTeamId() {
        return getLong(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, -1);
    }

    @Override
    public String getKnownSince() {
        return getString(TeambrellaModel.ATTR_DATA_KNOWN_SINCE);
    }

    @Override
    public String getAddress() {
        return getString(TeambrellaModel.ATTR_DATA_ADDRESS);
    }

    @Override
    public boolean isDefault() {
        return getBoolean(TeambrellaModel.ATTR_DATA_IS_DEFAULT, false);
    }
}
