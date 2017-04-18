package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITx;

/**
 * Json Transaction
 */
public class JsonTX extends JsonWrapper implements ITx {

    public JsonTX(JsonObject object) {
        super(object);
    }

    @Override
    public String getId() {
        return getString(TeambrellaModel.ATTR_DATA_ID);
    }

    @Override
    public long getTeammateId() {
        return getLong(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, -1);
    }

    @Override
    public float getBTCAmount() {
        return getFloat(TeambrellaModel.ATTR_DATA_BTC_AMOUNT, 0f);
    }

    @Override
    public long getClaimId() {
        return getLong(TeambrellaModel.ATTR_DATA_CLAIM_ID, -1);
    }

    @Override
    public long getClaimTeammateId() {
        return getLong(TeambrellaModel.ATTR_DATA_CLAIM_TEAMMATE_ID, -1);
    }

    @Override
    public int getKind() {
        return getInt(TeambrellaModel.ATTR_DATA_KIND, -1);
    }

    @Override
    public int getState() {
        return getInt(TeambrellaModel.ATTR_DATA_STATE, -1);
    }

    @Override
    public String getInitiatedTime() {
        return getString(TeambrellaModel.ATTR_DATA_INITIATED_TIME);
    }
}
