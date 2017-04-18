package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ICosigner;

/**
 * Json Cosigner
 */
public class JsonCosigner extends JsonWrapper implements ICosigner {

    public JsonCosigner(JsonObject object) {
        super(object);
    }

    @Override
    public long getTeammateId() {
        return getLong(TeambrellaModel.ATTR_DATA_TEAM_ID, -1);
    }

    @Override
    public String getAddressId() {
        return getString(TeambrellaModel.ATTR_DATA_TEAMMATE_ID);
    }

    @Override
    public int getKeyOrder() {
        return getInt(TeambrellaModel.ATTR_DATA_KEY_ORDER, -1);
    }
}
