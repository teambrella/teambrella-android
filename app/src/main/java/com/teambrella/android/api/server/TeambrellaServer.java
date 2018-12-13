package com.teambrella.android.api.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.bumptech.glide.load.model.LazyHeaders;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.api.TeambrellaAPI;
import com.teambrella.android.api.TeambrellaClientException;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;
import com.teambrella.android.util.log.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Teambrella API Provider
 */
public class TeambrellaServer {

    private static final String LOG_TAG = TeambrellaServer.class.getSimpleName();
    private static final String SHARED_PREFS_NAME = "teambrella_api";
    private static final String TIMESTAMP_KEY = "timestamp";
    public static final String AUTHORITY = BuildConfig.AUTHORITY;
    public static final String SCHEME = BuildConfig.SCHEME;


    /**
     * Shared preference
     */
    private final SharedPreferences mPreferences;


    /**
     * Teambrella API
     */
    private final TeambrellaAPI mAPI;

    /**
     * Key
     */
    private final ECKey mKey;


    private final String mDeviceCode;

    private final int mMask;

    /**
     * Constructor.
     *
     * @param context to use
     */
    public TeambrellaServer(Context context, String password, String deviceCode, int mask) {


        mPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor())
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build().toString())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        mAPI = retrofit.create(TeambrellaAPI.class);
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, password);
        mKey = dpk.getKey();
        mDeviceCode = deviceCode != null ? deviceCode : "";
        mMask = mask;
    }


    public Observable<JsonObject> requestObservable(Uri uri, JsonObject requestData) {
        return getObservableObject(uri, getRequestBody(uri, requestData))
                .map(jsonObjectResponse -> checkResponse(uri, jsonObjectResponse))
                .doOnNext(jsonObject -> checkStatus(uri, jsonObject))
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof TeambrellaServerException) {
                        if (((TeambrellaServerException) throwable).getErrorCode() == TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH) {
                            return getObservableObject(uri, getRequestBody(uri, requestData))
                                    .map(jsonObjectResponse -> checkResponse(uri, jsonObjectResponse))
                                    .doOnNext(jsonObject -> checkStatus(uri, jsonObject));
                        } else {
                            Log.reportNonFatal(LOG_TAG, throwable);
                            return Observable.error(throwable);
                        }
                    }
                    Exception exception = new TeambrellaClientException(uri, throwable.getMessage(), throwable);
                    Log.reportNonFatal(LOG_TAG, exception);
                    return Observable.error(exception);
                });
    }

    public LazyHeaders getHeaders() {
        Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        String publicKey = Utils.HEX.encode(ECKey.compressPoint(mKey.getPubKeyPoint()).getEncoded());
        return new LazyHeaders.Builder()
                .addHeader("t", Long.toString(timestamp))
                .addHeader("key", publicKey)
                .addHeader("sig", () -> mKey.signMessage(Long.toString(timestamp)))
                .addHeader("clientVersion", BuildConfig.VERSION_NAME)
                .addHeader("deviceId", mDeviceCode)
                .addHeader("deviceToken", FirebaseInstanceId.getInstance().getToken())
                .addHeader("info", Integer.toString(mMask) + ";" + Build.VERSION.RELEASE + ";" + Build.MANUFACTURER + " " + Build.MODEL)
                .build();
    }


    private JsonObject getRequestBody(Uri uri, JsonObject body) {

        JsonObject requestBody = body != null ? body : new JsonObject();
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST:
                int teamId = TeambrellaUris.getTeamId(uri);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, teamId);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                String sortedByRiskValue = uri.getQueryParameter(TeambrellaUris.KEY_SORTED_BY_RISK);
                boolean sortedByRisk = sortedByRiskValue != null && Boolean.parseBoolean(sortedByRiskValue);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SORTED_BY_RISK, sortedByRisk);
                break;
            case TeambrellaUris.USER_RATING:
                String optIn = uri.getQueryParameter(TeambrellaUris.KEY_OPT_IN);
                if (optIn != null) {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OPT_INTO, Boolean.parseBoolean(optIn));
                }
            case TeambrellaUris.MY_PROXIES:
            case TeambrellaUris.PROXY_FOR: {
                teamId = Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, teamId);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
            }
            break;
            case TeambrellaUris.TEAMMATES_ONE: {
                Pair<Integer, String> ids = TeambrellaUris.getTeamAndTeammateId(uri);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, ids.first);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, ids.second);
            }
            break;
            case TeambrellaUris.CLAIMS_LIST:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                String teammateIdParam = uri.getQueryParameter(TeambrellaUris.KEY_TEAMMATE_ID);
                if (teammateIdParam != null) {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAMMATE_ID_FILTER, Integer.parseInt(teammateIdParam));
                }
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                break;
            case TeambrellaUris.CLAIMS_ONE:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_ID)));
                break;
            case TeambrellaUris.CLAIMS_CHAT:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_CLAIM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_ID)));
                String sinceParam = uri.getQueryParameter(TeambrellaUris.KEY_SINCE);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SINCE, sinceParam != null ? Long.parseLong(sinceParam) : null);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                break;
            case TeambrellaUris.TEAMMATE_CHAT:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                sinceParam = uri.getQueryParameter(TeambrellaUris.KEY_SINCE);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SINCE, sinceParam != null ? Long.parseLong(sinceParam) : null);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                break;
            case TeambrellaUris.FEED_CHAT:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                sinceParam = uri.getQueryParameter(TeambrellaUris.KEY_SINCE);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SINCE, sinceParam != null ? Long.parseLong(sinceParam) : null);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
            case TeambrellaUris.NEW_POST:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_NEW_POST_ID, uri.getQueryParameter(TeambrellaUris.KEY_POST_ID));

                String message = uri.getQueryParameter(TeambrellaUris.KEY_TEXT);
                if (message != null) {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEXT, message);
                }
                String images = uri.getQueryParameter(TeambrellaUris.KEY_IMAGES);
                if (images != null) {
                    requestBody.add(TeambrellaModel.ATTR_REQUEST_IMAGES, new Gson().fromJson(images, JsonElement.class));
                }
                break;
            case TeambrellaUris.DELETE_POST:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                break;
            case TeambrellaUris.SET_CLAIM_VOTE: {
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_CLAIM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_ID)));
                float vote = Float.parseFloat(uri.getQueryParameter(TeambrellaUris.KEY_VOTE)) / 100;
                if (vote < 0) {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_VOTE, (Number) null);
                } else {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_VOTE, vote);
                }
            }
            break;
            case TeambrellaUris.SET_TEAMMATE_VOTE:
                Double vote = Double.parseDouble(uri.getQueryParameter(TeambrellaUris.KEY_VOTE));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAMMATE_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_ID)));
                if (vote > 0) {
                    requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_VOTE, vote);
                } else {
                    requestBody.add(TeambrellaModel.ATTR_REQUEST_MY_VOTE, null);
                }
                break;
            case TeambrellaUris.GET_HOME:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                break;
            case TeambrellaUris.GET_FEED:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                break;

            case TeambrellaUris.SET_MY_PROXY:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_ADD, Boolean.parseBoolean(uri.getQueryParameter(TeambrellaUris.KEY_ADD)));
                break;

            case TeambrellaUris.SET_PROXY_POSITION:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_POSITION, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_POSITION)));
                break;
            case TeambrellaUris.GET_COVERAGE_FOR_DATE:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_DATE, uri.getQueryParameter(TeambrellaUris.KEY_DATE));
                break;
            case TeambrellaUris.NEW_CLAIM:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_INCIDENT_DATE, uri.getQueryParameter(TeambrellaUris.KEY_DATE));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_EXPENSES, Float.parseFloat(uri.getQueryParameter(TeambrellaUris.KEY_EXPENSES)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MESSAGE, uri.getQueryParameter(TeambrellaUris.KEY_MESSAGE));
                requestBody.add(TeambrellaModel.ATTR_REQUEST_IMAGES, new Gson().fromJson(uri.getQueryParameter(TeambrellaUris.KEY_IMAGES), JsonElement.class));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_ADDRESS, uri.getQueryParameter(TeambrellaUris.KEY_ADDRESS));
                break;
            case TeambrellaUris.NEW_CHAT:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEXT, uri.getQueryParameter(TeambrellaUris.KEY_MESSAGE));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TITLE, uri.getQueryParameter(TeambrellaUris.KEY_TITLE));
                break;
            case TeambrellaUris.CONVERSATION_CHAT:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                sinceParam = uri.getQueryParameter(TeambrellaUris.KEY_SINCE);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SINCE, sinceParam != null ? Long.parseLong(sinceParam) : null);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
                break;
            case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEXT, uri.getQueryParameter(TeambrellaUris.KEY_MESSAGE));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TO_USER_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_NEW_MESSAGE_ID, uri.getQueryParameter(TeambrellaUris.KEY_POST_ID));
                break;
            case TeambrellaUris.APPLICATION_VOTES:
                String teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                String teammateIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAMMATE_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAMMATE_ID, Integer.parseInt(teammateIdString));
                break;
            case TeambrellaUris.CLAIMS_VOTES:
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                String claimIdString = uri.getQueryParameter(TeambrellaUris.KEY_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_CLAIM_ID, Integer.parseInt(claimIdString));
                break;
            case TeambrellaUris.WALLET: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
            }
            break;
            case TeambrellaUris.WITHDRAWALS: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
            }
            break;
            case TeambrellaUris.NEW_WITHDRAW: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TO_ADDRESS, uri.getQueryParameter(TeambrellaUris.KEY_ADDRESS));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_AMOUNT, Double.parseDouble(uri.getQueryParameter(TeambrellaUris.KEY_AMOUNT)));
            }
            break;
            case TeambrellaUris.MUTE: {
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_IS_MUTED, Boolean.parseBoolean(uri.getQueryParameter(TeambrellaUris.KEY_MUTED)));
            }
            break;
            case TeambrellaUris.WALLET_TRANSACTIONS: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
            }
            break;

            case TeambrellaUris.GET_TEAM_NOTIFICATION_SETTINGS: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
            }
            break;
            case TeambrellaUris.SET_TEAM_NOTIFICATION_SETTINGS: {
                teamIdString = uri.getQueryParameter(TeambrellaUris.KEY_TEAM_ID);
                String notificationString = uri.getQueryParameter(TeambrellaUris.KEY_NOTIFICATION_SETTING);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, Integer.parseInt(teamIdString));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_NEW_TEAMMATES_NOTIFICATION, Integer.parseInt(notificationString));
            }
            case TeambrellaUris.GET_TOPIC_PIN: {
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
            }
            break;
            case TeambrellaUris.SET_TOPIC_PIN: {
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_PIN, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_MY_PIN)));
            }
            break;
            case TeambrellaUris.SET_POST_LIKE: {
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_POST_ID, uri.getQueryParameter(TeambrellaUris.KEY_POST_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_LIKE, uri.getQueryParameter(TeambrellaUris.KEY_MY_LIKE));
            }
            break;
            case TeambrellaUris.ME_REGISTER_USER:
            case TeambrellaUris.ME_UPDATES:
            case TeambrellaUris.ME_REGISTER_FACEBOOK_KEY:
            case TeambrellaUris.ME_REGISTER_AUTH0_KEY:
            case TeambrellaUris.MY_TEAMS:
            case TeambrellaUris.NEW_FILE:
            case TeambrellaUris.SET_AVATAR:
            case TeambrellaUris.INBOX:
            case TeambrellaUris.DEMO_TEAMS:
            case TeambrellaUris.GET_DEMO_TEAMS_SUR:
            case TeambrellaUris.DEBUG_DB:
            case TeambrellaUris.DEBUG_LOG:
            case TeambrellaUris.GET_ME:
            case TeambrellaUris.JOIN_GET_WELCOME:
                break;
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
        return requestBody;
    }

    private Observable<Response<JsonObject>> getObservableObject(Uri uri, JsonObject requestBody) {
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST:
                return mAPI.getTeammateList(requestBody);
            case TeambrellaUris.TEAMMATES_ONE:
                return mAPI.getTeammateOne(requestBody);
            case TeambrellaUris.ME_UPDATES:
                return mAPI.getUpdates(requestBody);
            case TeambrellaUris.ME_REGISTER_FACEBOOK_KEY:
                String facebookToken = uri.getQueryParameter(TeambrellaUris.KEY_FACEBOOK_TOKEN);
                String sigOfPublicKey = uri.getQueryParameter(TeambrellaUris.KEY_SIG_OF_PUBLIC_KEY);
                return mAPI.registerKey(facebookToken, sigOfPublicKey);

            case TeambrellaUris.ME_REGISTER_AUTH0_KEY: {
                String auth0Token = uri.getQueryParameter(TeambrellaUris.KEY_AUTH0_TOKEN);
                String signature = uri.getQueryParameter(TeambrellaUris.KEY_SIG_OF_PUBLIC_KEY);
                return mAPI.registerAuth0Key(auth0Token, signature);
            }

            case TeambrellaUris.ME_REGISTER_USER: {
                String signature = uri.getQueryParameter(TeambrellaUris.KEY_SIG_OF_PUBLIC_KEY);
                return mAPI.registerUser(signature, requestBody);
            }

            case TeambrellaUris.CLAIMS_LIST:
                return mAPI.getClaimsList(requestBody);
            case TeambrellaUris.CLAIMS_ONE:
                return mAPI.getClaim(requestBody);
            case TeambrellaUris.CLAIMS_CHAT:
                return mAPI.getClaimChat(requestBody);
            case TeambrellaUris.NEW_POST:
                return mAPI.newPost(requestBody);
            case TeambrellaUris.DELETE_POST:
                return mAPI.deletePost(requestBody);
            case TeambrellaUris.MY_TEAMS:
                return mAPI.getTeams();
            case TeambrellaUris.SET_CLAIM_VOTE:
                return mAPI.setClaimVote(requestBody);
            case TeambrellaUris.SET_TEAMMATE_VOTE:
                return mAPI.setTeammateVote(requestBody);
            case TeambrellaUris.GET_HOME:
                return mAPI.getHome(requestBody);
            case TeambrellaUris.GET_FEED:
                return mAPI.getFeed(requestBody);
            case TeambrellaUris.FEED_CHAT:
                return mAPI.getFeedChat(requestBody);
            case TeambrellaUris.TEAMMATE_CHAT:
                return mAPI.getTeammateChat(requestBody);
            case TeambrellaUris.MY_PROXIES:
                return mAPI.getMyProxies(requestBody);
            case TeambrellaUris.USER_RATING:
                return mAPI.getUserRating(requestBody);
            case TeambrellaUris.PROXY_FOR:
                return mAPI.getProxyFor(requestBody);
            case TeambrellaUris.SET_MY_PROXY:
                return mAPI.setMyProxy(requestBody);
            case TeambrellaUris.SET_PROXY_POSITION:
                return mAPI.setProxyPosition(requestBody);
            case TeambrellaUris.NEW_FILE:
                String camUsed = uri.getQueryParameter(TeambrellaUris.KEY_CAM);
                if (camUsed != null)
                    return mAPI.newFileCam("X", RequestBody.create(MediaType.parse("image/jpeg"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
                else
                    return mAPI.newFile(RequestBody.create(MediaType.parse("image/jpeg"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
            case TeambrellaUris.DEBUG_DB:
                return mAPI.debugDB(RequestBody.create(MediaType.parse("application/octet-stream"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
            case TeambrellaUris.DEBUG_LOG:
                return mAPI.debugLog(RequestBody.create(MediaType.parse("application/octet-stream"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
            case TeambrellaUris.GET_COVERAGE_FOR_DATE:
                return mAPI.getCoverageForDate(requestBody);
            case TeambrellaUris.NEW_CLAIM:
                return mAPI.newClaim(requestBody);
            case TeambrellaUris.NEW_CHAT:
                return mAPI.newChat(requestBody);
            case TeambrellaUris.INBOX:
                return mAPI.getInbox(requestBody);
            case TeambrellaUris.CONVERSATION_CHAT:
                return mAPI.getConversationChat(requestBody);
            case TeambrellaUris.NEW_PRIVATE_MESSAGE:
                return mAPI.newConversationMessage(requestBody);
            case TeambrellaUris.APPLICATION_VOTES:
                return mAPI.getApplicationVotes(requestBody);
            case TeambrellaUris.CLAIMS_VOTES:
                return mAPI.getClaimVotes(requestBody);
            case TeambrellaUris.WALLET:
                return mAPI.getWallet(requestBody);
            case TeambrellaUris.DEMO_TEAMS:
                return mAPI.getDemoTeams(uri.getQueryParameter(TeambrellaUris.KEY_LANGUAGE));
            case TeambrellaUris.GET_DEMO_TEAMS_SUR:
                return mAPI.getDemoTeamsSur(Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_STATUS)));
            case TeambrellaUris.WITHDRAWALS:
                return mAPI.getWithdrawls(requestBody);
            case TeambrellaUris.NEW_WITHDRAW:
                return mAPI.newWithdraw(requestBody);
            case TeambrellaUris.MUTE:
                return mAPI.setChatMuted(requestBody);
            case TeambrellaUris.WALLET_TRANSACTIONS:
                return mAPI.getWalletTransactions(requestBody);
            case TeambrellaUris.GET_ME:
                return mAPI.getMe(requestBody);
            case TeambrellaUris.GET_TEAM_NOTIFICATION_SETTINGS:
                return mAPI.getTeamNotificationSettings(requestBody);
            case TeambrellaUris.SET_TEAM_NOTIFICATION_SETTINGS:
                return mAPI.setTeamNotificationSettings(requestBody);
            case TeambrellaUris.SET_AVATAR:
                return mAPI.setAvatar(RequestBody.create(MediaType.parse("image/jpeg"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
            case TeambrellaUris.GET_TOPIC_PIN:
                return mAPI.getTopicPin(requestBody);
            case TeambrellaUris.SET_TOPIC_PIN:
                return mAPI.setTopicPin(requestBody);
            case TeambrellaUris.SET_POST_LIKE:
                return mAPI.setPostLike(requestBody);
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
    }


    private JsonObject checkResponse(Uri uri, Response<JsonObject> response) throws TeambrellaException {
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new TeambrellaException(uri);
    }

    private boolean checkStatus(Uri uri, JsonObject responseBody) throws TeambrellaServerException {
        JsonObject status = responseBody.getAsJsonObject(TeambrellaModel.ATTR_STATUS);
        if (status != null) {
            JsonElement resultCodeElement = status.get(TeambrellaModel.ATTR_STATUS_RESULT_CODE);
            int resultCode = !resultCodeElement.isJsonNull() ? resultCodeElement.getAsInt() : TeambrellaModel.VALUE_STATUS_RESULT_CODE_FATAL;
            JsonElement errorMessageElement = status.get(TeambrellaModel.ATTR_STATUS_ERROR_MESSAGE);
            String errorMessage = errorMessageElement == null || errorMessageElement.isJsonNull() ? null : errorMessageElement.getAsString();
            JsonElement timestampElement = status.get(TeambrellaModel.ATTR_STATUS_TIMESTAMP);
            long timestamp = timestampElement.isJsonNull() ? 0 : timestampElement.getAsLong();

            if (timestamp > 0) {
                updateTimestamp(timestamp);
            } else {
                throw new TeambrellaServerException(uri, TeambrellaModel.VALUE_STATUS_RESULT_CODE_FATAL, "Something went wrong", 0);
            }
            if (resultCode != 0) {
                switch (TeambrellaUris.sUriMatcher.match(uri)) {
                    case TeambrellaUris.DELETE_POST:
                        return true;
                    default:
                        throw new TeambrellaServerException(uri, resultCode, errorMessage, timestamp);
                }
            }
        }
        return true;
    }

    private void updateTimestamp(long timestamp) {
        mPreferences.edit().putLong(TIMESTAMP_KEY, timestamp).apply();
    }


    private class HeaderInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
            String publicKey = Utils.HEX.encode(ECKey.compressPoint(mKey.getPubKeyPoint()).getEncoded());
            String signature = mKey.signMessage(Long.toString(timestamp));
            Request newRequest = chain.request().newBuilder()
                    .addHeader("t", Long.toString(timestamp))
                    .addHeader("key", publicKey)
                    .addHeader("sig", signature)
                    .addHeader("clientVersion", BuildConfig.VERSION_NAME)
                    .addHeader("deviceId", mDeviceCode)
                    .addHeader("deviceToken", FirebaseInstanceId.getInstance().getToken())
                    .addHeader("info", Integer.toString(mMask) + ";" + Build.VERSION.RELEASE + ";" + Build.MANUFACTURER + " " + Build.MODEL)
                    .build();
            return chain.proceed(newRequest);
        }
    }


    public TeambrellaSocketClient createSocketClient(URI uri, SocketClientListener listener, long lastNotificationTimeStamp) {
        Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        String publicKey = Utils.HEX.encode(ECKey.compressPoint(mKey.getPubKeyPoint()).getEncoded());
        String signature = mKey.signMessage(Long.toString(timestamp));
        HashMap<String, String> headers = new HashMap<>();
        headers.put("t", Long.toString(timestamp));
        headers.put("key", publicKey);
        headers.put("sig", signature);
        headers.put("clientVersion", BuildConfig.VERSION_NAME);
        headers.put("deviceId", mDeviceCode);
        headers.put("deviceToken", FirebaseInstanceId.getInstance().getToken());
        headers.put("info", Integer.toString(mMask) + ";" + Build.VERSION.RELEASE + ";" + Build.MANUFACTURER + " " + Build.MODEL);
        return new TeambrellaSocketClient(uri, headers, listener, lastNotificationTimeStamp);
    }


    public interface SocketClientListener {

        void onOpen();

        void onMessage(String message);

        void onClose(int code, String reason, boolean remote);

        void onError(Exception ex);
    }

    public static class TeambrellaSocketClient extends WebSocketClient {

        private final SocketClientListener mListener;
        private final long mLastNotificationTimeStamp;


        TeambrellaSocketClient(URI serverUri, Map<String, String> httpHeaders, SocketClientListener listener, long lastNotificationTimeStamp) {
            super(serverUri, new Draft_6455(), httpHeaders, 1000 * 30);
            this.mListener = listener;
            mLastNotificationTimeStamp = lastNotificationTimeStamp;
        }


        @Override
        public void connect() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, null);
                setSocket(context.getSocketFactory().createSocket());
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.connect();
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            send("0" + (mLastNotificationTimeStamp > 0 ? (";" + mLastNotificationTimeStamp) : ""));
            mListener.onOpen();
        }

        @Override
        public void onMessage(String message) {
            mListener.onMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            mListener.onClose(code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            mListener.onError(ex);
        }
    }


}
