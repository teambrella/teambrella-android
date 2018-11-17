package com.teambrella.android.api.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

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
    return getString(key, null);
  }

  public String getString(String key, String defaultValue) {
    JsonElement value = mObject.get(key);
    if (value != null && !value.isJsonNull()) {
      return value.getAsString();
    }
    return defaultValue;
  }

  /*
   * Get Integer property
   */
  public int getInt(String key) {
    return getInt(key, 0);
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

  public boolean hasValue(String key) {
    JsonElement value = mObject.get(key);
    return (value != null && !value.isJsonNull());
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


  public float getFloat(String key) {
    return getFloat(key, 0f);
  }


  public float getFloat(String key, float defaultValue) {
    JsonElement value = mObject.get(key);
    if (value != null && !value.isJsonNull()) {
      return value.getAsFloat();
    }
    return defaultValue;
  }

  public double getDouble(String key) {
    return getDouble(key, 0f);
  }

  public double getDouble(String key, double defaultValue) {
    JsonElement value = mObject.get(key);
    if (value != null && !value.isJsonNull()) {
      return value.getAsDouble();
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


  public ArrayList<JsonWrapper> getArray(String key) {
    ArrayList<JsonWrapper> array = new ArrayList<>();
    JsonElement value = mObject.get(key);
    if (value != null && !value.isJsonNull()) {
      JsonArray jsonArray = value.getAsJsonArray();
      for (JsonElement element : jsonArray) {
        array.add(new JsonWrapper(element.getAsJsonObject()));
      }
    }

    return array;
  }


  public JsonWrapper getObject(String key) {
    JsonElement value = mObject.get(key);
    if (value != null && !value.isJsonNull()) {
      return new JsonWrapper(value.getAsJsonObject());
    }
    return null;
  }

  public JsonObject getObject() {
    return mObject;
  }
}
