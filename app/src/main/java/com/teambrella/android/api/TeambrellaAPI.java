package com.teambrella.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
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
    Observable<Response<JsonObject>> registerKey(@Query("facebookToken") String facebookToken, @Query("sigOfPublicKey") String sigOfPublicKey);

    @Headers("Content-Type: application/json")
    @POST("me/registerKey")
    Observable<Response<JsonObject>> registerAuth0Key(@Query("auth0Token") String auth0Token, @Query("sigOfPublicKey") String sigOfPublicKey);


    @Headers("Content-Type: application/json")
    @POST("join/registerKey")
    Observable<Response<JsonObject>> registerUser(@Query("sigOfPublicKey") String sigOfPublicKey, @Body JsonElement body);


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
    @POST("post/delPost")
    Observable<Response<JsonObject>> deletePost(@Body JsonElement body);

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

    @Headers("Content-Type: image/jpeg")
    @POST("post/newUpload")
    Observable<Response<JsonObject>> newFileCam(@Query("cam") String camUsed, @Body RequestBody body);

    @Headers("Content-Type: application/octet-stream")
    @POST("me/debugDB")
    Observable<Response<JsonObject>> debugDB(@Body RequestBody body);


    @Headers("Content-Type: application/octet-stream")
    @POST("me/debugLog")
    Observable<Response<JsonObject>> debugLog(@Body RequestBody body);


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

    @Headers("Content-Type: image/jpeg")
    @POST("privatemessage/newUpload")
    Observable<Response<JsonObject>> newConversationFile(@Body RequestBody body);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("teammate/getAllVotesList")
    Observable<Response<JsonObject>> getApplicationVotes(@Body JsonElement body);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("claim/getAllVotesList")
    Observable<Response<JsonObject>> getClaimVotes(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("wallet/getOne")
    Observable<Response<JsonObject>> getWallet(@Body JsonElement body);


    @Headers("Content-Type: application/json")
    @POST("demo/getTeams/{language}")
    Observable<Response<JsonObject>> getDemoTeams(@Path("language") String language);


    @Headers("Content-Type: application/json")
    @POST("demo/getTeamsSur/{status}")
    Observable<Response<JsonObject>> getDemoTeamsSur(@Path("status") int language);


    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("wallet/getWithdraw")
    Observable<Response<JsonObject>> getWithdrawls(@Body JsonElement body);
    
    @SuppressWarnings("SpellCheckingInspection")
    @Headers("Content-Type: application/json")
    @POST("wallet/newWithdraw")
    Observable<Response<JsonObject>> newWithdraw(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("feed/setIsMuted")
    Observable<Response<JsonObject>> setChatMuted(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("wallet/getMyTxList")
    Observable<Response<JsonObject>> getWalletTransactions(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("me/getMe")
    Observable<Response<JsonObject>> getMe(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("feed/getMySettings")
    Observable<Response<JsonObject>> getTeamNotificationSettings(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("feed/setMySettings")
    Observable<Response<JsonObject>> setTeamNotificationSettings(@Body JsonElement body);

    @Headers("Content-Type: image/jpeg")
    @POST("me/setAvatar")
    Observable<Response<JsonObject>> setAvatar(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("feed/getPin")
    Observable<Response<JsonObject>> getTopicPin(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("feed/setPin")
    Observable<Response<JsonObject>> setTopicPin(@Body JsonElement body);

    @Headers("Content-Type: application/json")
    @POST("vote/setPostLike")
    Observable<Response<JsonObject>> setPostLike(@Body JsonElement body);
}