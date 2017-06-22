package com.teambrella.android.api.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaAPI;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Teambrella API Provider
 */
public class TeambrellaServer {

    private static final String SHARED_PREFS_NAME = "teambrella_api";
    private static final String TIMESTAMP_KEY = "timestamp";
    //public static final String AUTHORITY = "http://94.72.4.72/";
    public static final String AUTHORITY = "http://192.168.0.222/";

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


    /**
     * Constructor.
     *
     * @param context to use
     */
    public TeambrellaServer(Context context, String password) {


        mPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AUTHORITY)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        mAPI = retrofit.create(TeambrellaAPI.class);
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, password);
        mKey = dpk.getKey();
    }


    public Observable<JsonObject> requestObservable(Uri uri, JsonObject requestData) {
        return getObservableObject(uri, getRequestBody(uri, requestData))
                .map(this::checkResponse)
                .doOnNext(this::checkStatus)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof TeambrellaServerException) {
                        if (((TeambrellaServerException) throwable).getErrorCode() == TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH) {
                            return getObservableObject(uri, getRequestBody(uri, requestData))
                                    .map(this::checkResponse)
                                    .doOnNext(TeambrellaServer.this::checkStatus);
                        }
                    }
                    return Observable.error(throwable);
                });
    }

    public OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder().addInterceptor(new HeaderInterceptor()).build();
    }


    private JsonObject getRequestBody(Uri uri, JsonObject body) {

        JsonObject requestBody = body != null ? body : new JsonObject();
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST: {
                int teamId = TeambrellaUris.getTeamId(uri);
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
            case TeambrellaUris.NEW_POST:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TOPIC_ID, uri.getQueryParameter(TeambrellaUris.KEY_ID));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEXT, uri.getQueryParameter(TeambrellaUris.KEY_TEXT));
                break;
            case TeambrellaUris.ME_UPDATES:
            case TeambrellaUris.ME_REGISTER_KEY:
                break;
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
        return requestBody;
    }

    private Observable<Response<JsonObject>> getObservableObject(Uri uri, JsonObject requestBody) {
        Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        String publicKey = mKey.getPublicKeyAsHex();
        String signature = mKey.signMessage(Long.toString(timestamp));
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_AVATAR_SIZE, 256);
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST:
                return mAPI.getTeammateList(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.TEAMMATES_ONE:
                return mAPI.getTeammateOne(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.ME_UPDATES:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TIMESTAMP, timestamp);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_PUBLIC_KEY, publicKey);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SIGNATURE, signature);
                return mAPI.getUpdates(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.ME_REGISTER_KEY:
                String facebookToken = uri.getQueryParameter(TeambrellaUris.KEY_FACEBOOK_TOKEN);
                return mAPI.registerKey(timestamp, publicKey, signature, facebookToken);
            case TeambrellaUris.CLAIMS_LIST:
                return mAPI.getClaimsList(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.CLAIMS_ONE:
                return mAPI.getClaim(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.CLAIMS_CHAT:
                return mAPI.getClaimChat(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.NEW_POST:
                return mAPI.newPost(timestamp, publicKey, signature, requestBody);
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
    }


    private JsonObject checkResponse(Response<JsonObject> response) throws TeambrellaException {
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new TeambrellaException();
    }

    private boolean checkStatus(JsonObject responseBody) throws TeambrellaServerException {
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
                throw new TeambrellaServerException(TeambrellaModel.VALUE_STATUS_RESULT_CODE_FATAL, "Something went wrong", 0);
            }
            if (resultCode != 0) {
                throw new TeambrellaServerException(resultCode, errorMessage, timestamp);
            }
        }
        return true;
    }

    private void updateTimestamp(long timestamp) {
        mPreferences.edit().putLong(TIMESTAMP_KEY, timestamp).apply();
    }


    private class HeaderInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
            String publicKey = mKey.getPublicKeyAsHex();
            String signature = mKey.signMessage(Long.toString(timestamp));
            Request newRequest = chain.request().newBuilder()
                    .addHeader("t", Long.toString(timestamp))
                    .addHeader("key", publicKey)
                    .addHeader("sig", signature)
                    .build();
            return chain.proceed(newRequest);
        }
    }


}
