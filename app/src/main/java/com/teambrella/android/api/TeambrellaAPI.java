package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Teambrella API
 */
interface TeambrellaAPI {

    @POST("me/getTimestamp")
    Call<JsonObject> getTimeStamp();

    @Headers("Content-Type: application/json")
    @POST("teammate/getOne")
    Call<JsonObject> getTeammateOne(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getList")
    Call<JsonObject> getTeammateList(@Body JsonElement body);
}