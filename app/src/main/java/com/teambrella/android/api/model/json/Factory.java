package com.teambrella.android.api.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;

/**
 * Factory
 */
public class Factory {

    public static <T extends JsonWrapper> T fromJson(JsonObject object, Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor(JsonObject.class).newInstance(object);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends JsonWrapper> T[] fromArray(JsonArray array, Class<T> tClass) {
        T[] result = (T[]) Array.newInstance(tClass, array.size());
        for (int i = 0; i < array.size(); i++) {
            result[i] = fromJson(array.get(i).getAsJsonObject(), tClass);
        }
        return result;
    }
}
