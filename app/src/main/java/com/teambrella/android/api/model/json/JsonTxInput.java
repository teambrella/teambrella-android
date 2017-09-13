package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.ITxInput;

/**
 * Json Tx Input
 */
public class JsonTxInput extends JsonWrapper implements ITxInput {

    public JsonTxInput(JsonObject object) {
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
    public float getCryptoAmount() {
        return getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_AMOUNT, 0f);
    }

    @Override
    public String getPreviousTxId() {
        return getString(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_ID);
    }

    @Override
    public int getPreviousTxIndex() {
        return getInt(TeambrellaModel.ATTR_DATA_PREVIOUS_TX_INDEX, -1);
    }
}
