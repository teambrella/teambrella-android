package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.IMultisig;

/**
 * Json Multisig
 */
public class JsonMultisig extends JsonWrapper implements IMultisig {

    public JsonMultisig(JsonObject object) {
        super(object);
    }

    @Override
    public int getId() {
        return getInt(TeambrellaModel.ATTR_DATA_ID);
    }

    @Override
    public String getAddress() { return getString(TeambrellaModel.ATTR_DATA_ADDRESS); }

    @Override
    public String getTeammateId() {
        return getString(TeambrellaModel.ATTR_DATA_TEAMMATE_ID);
    }

    @Override
    public int getStatus() {
        return getInt(TeambrellaModel.ATTR_DATA_STATUS, -1);
    }

    @Override
    public String getCreatedDate() {
        return getString(TeambrellaModel.ATTR_DATA_DATE_CREATED);
    }
}
