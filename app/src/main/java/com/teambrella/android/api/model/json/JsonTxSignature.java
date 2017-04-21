package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITxSignature;

/**
 * Json Signature
 */
public class JsonTxSignature extends JsonWrapper implements ITxSignature {

    public JsonTxSignature(JsonObject object) {
        super(object);
    }

    @Override
    public String getId() {
        return getString(TeambrellaModel.ATTR_DATA_ID);
    }

    @Override
    public String getTxId() {
        return getString(TeambrellaModel.ATTR_DATA_TX_ID);
    }

    @Override
    public long getTeammateId() {
        return getLong(TeambrellaModel.ATTR_DATA_TEAMMATE_ID, 0);
    }

    @Override
    public String getSignature() {
        return getString(TeambrellaModel.ATTR_DATA_SIGNATURE);
    }
}
