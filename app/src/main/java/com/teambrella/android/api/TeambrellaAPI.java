package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
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
    Observable<JsonObject> getTimeStamp(@Header("t") long timestamp,
                                        @Header("key") String publicKey,
                                        @Header("sig") String signature);

    @Headers("Content-Type: application/json")
    @POST("teammate/getOne")
    Observable<JsonObject> getTeammateOne(@Header("t") long timestamp,
                                          @Header("key") String publicKey,
                                          @Header("sig") String signature,
                                          @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getList")
    Observable<JsonObject> getTeammateList(@Header("t") long timestamp,
                                           @Header("key") String publicKey,
                                           @Header("sig") String signature,
                                           @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/GetUpdates")
    Observable<JsonObject> getUpdates(@Header("t") long timestamp,
                                      @Header("key") String publicKey,
                                      @Header("sig") String signature,
                                      @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/registerKey")
    Observable<JsonObject> registerKey(@Header("t") long timestamp,
                                       @Header("key") String publicKey,
                                       @Header("sig") String signature,
                                       @Query("facebookToken") String facebookToken);
}