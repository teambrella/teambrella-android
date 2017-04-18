package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.IBTCAddress;

/**
 * Json BTCAddress
 */
public class JsonBTCAddress extends JsonWrapper implements IBTCAddress {

    public JsonBTCAddress(JsonObject object) {
        super(object);
    }

    @Override
    public String getAddress() {
        return getString(TeambrellaModel.ATTR_DATA_ADDRESS);
    }

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
