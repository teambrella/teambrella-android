package com.teambrella.android.api.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json Wrapper
 */
public class JsonWrapper {

    /**
     * Data object
     */
    protected final JsonObject mObject;

    public JsonWrapper(JsonObject object) {
        this.mObject = object;
    }

    /*
     * Get String property
     */
    public String getString(String key) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsString();
        }
        return null;
    }

    /*
     * Get Integer property
     */
    public int getInt(String key, int defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsInt();
        }
        return defaultValue;
    }


    /*
     * Get Long property
     */
    public long getLong(String key, long defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsLong();
        }
        return defaultValue;
    }

    /*
     * Get Boolean property
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsBoolean();
        }
        return defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsFloat();
        }
        return defaultValue;
    }

    public JsonArray getJsonArray(String key) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsJsonArray();
        }
        return null;
    }


    public JsonWrapper getObject(String key) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return new JsonWrapper(value.getAsJsonObject());
        }
        return null;
    }
}
