package com.teambrella.android.blockchain;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Scan<T> {
    @SerializedName("result")
    public T result;

    @SerializedName("error")
    public JsonObject error;
}
