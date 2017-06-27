package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.Response;
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
    Observable<Response<JsonObject>> getTimeStamp(@Header("t") long timestamp,
                                                  @Header("key") String publicKey,
                                                  @Header("sig") String signature);

    @Headers("Content-Type: application/json")
    @POST("teammate/getOne")
    Observable<Response<JsonObject>> getTeammateOne(@Header("t") long timestamp,
                                                    @Header("key") String publicKey,
                                                    @Header("sig") String signature,
                                                    @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getList")
    Observable<Response<JsonObject>> getTeammateList(@Header("t") long timestamp,
                                                     @Header("key") String publicKey,
                                                     @Header("sig") String signature,
                                                     @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("claim/getList")
    Observable<Response<JsonObject>> getClaimsList(@Header("t") long timestamp,
                                                   @Header("key") String publicKey,
                                                   @Header("sig") String signature,
                                                   @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("claim/getOne")
    Observable<Response<JsonObject>> getClaim(@Header("t") long timestamp,
                                              @Header("key") String publicKey,
                                              @Header("sig") String signature,
                                              @Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("me/GetUpdates")
    Observable<Response<JsonObject>> getUpdates(@Header("t") long timestamp,
                                                @Header("key") String publicKey,
                                                @Header("sig") String signature,
                                                @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/registerKey")
    Observable<Response<JsonObject>> registerKey(@Header("t") long timestamp,
                                                 @Header("key") String publicKey,
                                                 @Header("sig") String signature,
                                                 @Query("facebookToken") String facebookToken);


    @Headers("Content-Type: application/json")
    @POST("claim/getChat")
    Observable<Response<JsonObject>> getClaimChat(@Header("t") long timestamp,
                                                  @Header("key") String publicKey,
                                                  @Header("sig") String signature,
                                                  @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("post/newPost")
    Observable<Response<JsonObject>> newPost(@Header("t") long timestamp,
                                             @Header("key") String publicKey,
                                             @Header("sig") String signature,
                                             @Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/getTeams")
    Observable<Response<JsonObject>> getTeams(@Header("t") long timestamp,
                                              @Header("key") String publicKey,
                                              @Header("sig") String signature);

    @Headers("Content-Type: application/json")
    @POST("claim/setVote")
    Observable<Response<JsonObject>> setClaimVote(@Header("t") long timestamp,
                                                  @Header("key") String publicKey,
                                                  @Header("sig") String signature,
                                                  @Body JsonElement body);

}