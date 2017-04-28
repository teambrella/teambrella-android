package com.teambrella.android.api.model.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json Wrapper
 */
class JsonWrapper {

    /**
     * Data object
     */
    protected final JsonObject mObject;

    JsonWrapper(JsonObject object) {
        this.mObject = object;
    }

    /*
     * Get String property
     */
    String getString(String key) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsString();
        }
        return null;
    }

    /*
     * Get Integer property
     */
    int getInt(String key, int defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsInt();
        }
        return defaultValue;
    }

    /*
     * Get Long property
     */
    long getLong(String key, long defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsLong();
        }
        return defaultValue;
    }

    /*
     * Get Boolean property
     */
    boolean getBoolean(String key, boolean defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsBoolean();
        }
        return defaultValue;
    }

    float getFloat(String key, float defaultValue) {
        JsonElement value = mObject.get(key);
        if (value != null && !value.isJsonNull()) {
            return value.getAsFloat();
        }
        return defaultValue;
    }
}
