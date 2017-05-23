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
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
        return getObservableObject(uri, getRequestBody(uri, requestData)).doOnNext(this::checkStatus);
    }


    private JsonObject getRequestBody(Uri uri, JsonObject body) {
        JsonObject requestBody = createBaseRequestBody(body);
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST: {
                int teamId = TeambrellaUris.getTeamId(uri);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, teamId);
            }
            break;
            case TeambrellaUris.TEAMMATES_ONE: {
                Pair<Integer, String> ids = TeambrellaUris.getTeamAndTeammateId(uri);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, ids.first);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_USER_ID, ids.second);
            }
            break;
            case TeambrellaUris.ME_UPDATES:
            case TeambrellaUris.ME_REGISTER_KEY:
                break;
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
        return requestBody;
    }

    private Observable<JsonObject> getObservableObject(Uri uri, JsonObject requestBody) {
        Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        String publicKey = mKey.getPublicKeyAsHex();
        String signature = mKey.signMessage(Long.toString(timestamp));
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST:
                return mAPI.getTeammateList(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.TEAMMATES_ONE:
                return mAPI.getTeammateOne(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.ME_UPDATES:
                return mAPI.getUpdates(timestamp, publicKey, signature, requestBody);
            case TeambrellaUris.ME_REGISTER_KEY:
                String facebookToken = uri.getQueryParameter(TeambrellaUris.KET_FACEBOOK_TOKEN);
                return mAPI.registerKey(timestamp, publicKey, signature, facebookToken);
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
    }

    /**
     * Create base request body
     *
     * @return request object
     */
    private JsonObject createBaseRequestBody(JsonObject requestBody) {
        return updateRequestBody(requestBody != null ? requestBody : new JsonObject());
    }


    /**
     * Create base request body
     *
     * @return request object
     */
    private JsonObject updateRequestBody(JsonObject requestBody) {
        long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TIMESTAMP, timestamp);
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_PUBLIC_KEY, mKey.getPublicKeyAsHex());
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SIGNATURE, mKey.signMessage(Long.toString(timestamp)));
        return requestBody;
    }

    private boolean checkStatus(JsonObject responseBody) throws TeambrellaServerException {
        JsonObject status = responseBody.getAsJsonObject(TeambrellaModel.ATTR_STATUS);
        if (status != null) {
            JsonElement resultCodeElement = status.get(TeambrellaModel.ATTR_STATUS_RESULT_CODE);
            int resultCode = !resultCodeElement.isJsonNull() ? resultCodeElement.getAsInt() : TeambrellaModel.VALUE_STATUS_RESULT_CODE_FATAL;
            JsonElement errorMessageElement = status.get(TeambrellaModel.ATTR_STATUS_ERROR_MESSAGE);
            String errorMessage = errorMessageElement.isJsonNull() ? null : errorMessageElement.getAsString();
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


}
