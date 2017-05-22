package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Teambrella API
 */
public interface TeambrellaAPI {

    @POST("me/getTimestamp")
    Call<JsonObject> getTimeStamp();

    @Headers("Content-Type: application/json")
    @POST("teammate/getOne")
    Call<JsonObject> getTeammateOne(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getList")
    Call<JsonObject> getTeammateList(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/GetUpdates")
    Call<JsonObject> getUpdates(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/registerKey")
    Call<JsonObject> registerKey(@Header("t") long timestamp,
                                 @Header("key") String publicKey,
                                 @Header("sig") String signature,
                                 @Query("facebookToken") String facebookToken);
}