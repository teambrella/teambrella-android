package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITxOutput;

/**
 * Json TX OutPut
 */
public class JsonTxOutput extends JsonWrapper implements ITxOutput {

    public JsonTxOutput(JsonObject object) {
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
    public String getPayToId() {
        return getString(TeambrellaModel.ATTR_DATA_PAY_TO_ID);
    }

    @Override
    public float getCryptoAmount() {
        return getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_AMOUNT, -1);
    }
}
