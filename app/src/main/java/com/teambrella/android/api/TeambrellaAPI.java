package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Teambrella API
 */
public interface TeambrellaAPI {

    @POST("me/getTimestamp")
    Observable<Response<JsonObject>> getTimeStamp();

    @Headers("Content-Type: application/json")
    @POST("teammate/getOne")
    Observable<Response<JsonObject>> getTeammateOne(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getList")
    Observable<Response<JsonObject>> getTeammateList(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("claim/getList")
    Observable<Response<JsonObject>> getClaimsList(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("claim/getOne")
    Observable<Response<JsonObject>> getClaim(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("me/GetUpdates")
    Observable<Response<JsonObject>> getUpdates(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/registerKey")
    Observable<Response<JsonObject>> registerKey(@Query("facebookToken") String facebookToken);


    @Headers("Content-Type: application/json")
    @POST("claim/getChat")
    Observable<Response<JsonObject>> getClaimChat(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/getChat")
    Observable<Response<JsonObject>> getTeammateChat(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("feed/getChat")
    Observable<Response<JsonObject>> getFeedChat(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("post/newPost")
    Observable<Response<JsonObject>> newPost(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/getTeams")
    Observable<Response<JsonObject>> getTeams();

    @Headers("Content-Type: application/json")
    @POST("claim/setVote")
    Observable<Response<JsonObject>> setClaimVote(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("teammate/setVote")
    Observable<Response<JsonObject>> setTeammateVote(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("feed/getHome")
    Observable<Response<JsonObject>> getHome(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("feed/getList")
    Observable<Response<JsonObject>> getFeed(@Body JsonElement body);

    @Multipart
    @POST("claim/newTempFile")
    Observable<Response<JsonObject>> newClaimFile(@Part MultipartBody.Part file);
}