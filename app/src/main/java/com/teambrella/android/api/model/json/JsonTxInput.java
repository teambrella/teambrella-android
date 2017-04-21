package com.teambrella.android.api.model.json;

import com.google.gson.JsonObject;
import com.teambrella.android.api.model.ITxInput;

/**
 * Tx Input
 */
public class JsonTxInput extends JsonWrapper implements ITxInput {

    public JsonTxInput(JsonObject object) {
        super(object);
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public long getTxId() {
        return 0;
    }

    @Override
    public float getBTCAmount() {
        return 0;
    }

    @Override
    public String getPreviousTxId() {
        return null;
    }

    @Override
    public int getPreviosTxIndex() {
        return 0;
    }
}
