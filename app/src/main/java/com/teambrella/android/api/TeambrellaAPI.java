package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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

    @Headers("Content-Type: application/json")
    @POST("proxy/getMyProxiesList")
    Observable<Response<JsonObject>> getMyProxies(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("proxy/getIAmProxyForList")
    Observable<Response<JsonObject>> getProxyFor(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("proxy/setOptIntoRating")
    Observable<Response<JsonObject>> getUserRating(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("proxy/setMyProxy")
    Observable<Response<JsonObject>> setMyProxy(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("proxy/setMyProxyPosition")
    Observable<Response<JsonObject>> setProxyPosition(@Body JsonElement body);


    @Headers("Content-Type: image/jpeg")
    @POST("post/newUpload")
    Observable<Response<JsonObject>> newFile(@Body RequestBody body);


    @Headers("Content-Type: application/json")
    @POST("me/getCoverageForDate")
    Observable<Response<JsonObject>> getCoverageForDate(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("claim/newClaim")
    Observable<Response<JsonObject>> newClaim(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("feed/newChat")
    Observable<Response<JsonObject>> newChat(@Body JsonElement body);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("privatemessage/getList")
    Observable<Response<JsonObject>> getInbox(@Body JsonElement body);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("privatemessage/getChat")
    Observable<Response<JsonObject>> getConversationChat(@Body JsonElement body);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("privatemessage/newMessage")
    Observable<Response<JsonObject>> newConversationMessage(@Body JsonElement body);
}