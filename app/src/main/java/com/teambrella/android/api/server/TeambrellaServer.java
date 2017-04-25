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
import com.teambrella.android.api.TeambrellaClientException;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Teambrella API Provider
 */
public class TeambrellaServer {

    private static final String SHARED_PREFS_NAME = "teambrella_api";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String PRIVATE_KEY = "Kxv2gGGa2ZW85b1LXh1uJSP3HLMV6i6qRxxStRhnDsawXDuMJadB";
    private static final String PUBLIC_KEY = "0203ca066905e668d1232a33bf5cce76ee1754b0a24ae9c28d20e13b069274742c";
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
    public TeambrellaServer(Context context) {
        mPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AUTHORITY)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        mAPI = retrofit.create(TeambrellaAPI.class);

        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, PRIVATE_KEY);
        mKey = dpk.getKey();
    }


    public JsonObject execute(Uri uri) throws TeambrellaException {
        try {
            return execute(getCallObject(uri, getRequestBody(uri)));
        } catch (TeambrellaServerException e) {
            switch (e.getErrorCode()) {
                case TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH:
                    return execute(uri);
                default:
                    throw e;

            }
        }
    }


    private JsonObject getRequestBody(Uri uri) {
        final JsonObject requestBody = createBaseRequestBody();
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
                break;
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
        return requestBody;
    }

    private Call<JsonObject> getCallObject(Uri uri, JsonObject requestBody) {
        switch (TeambrellaUris.sUriMatcher.match(uri)) {
            case TeambrellaUris.TEAMMATES_LIST:
                return mAPI.getTeammateList(requestBody);
            case TeambrellaUris.TEAMMATES_ONE:
                return mAPI.getTeammateOne(requestBody);
            case TeambrellaUris.ME_UPDATES:
                return mAPI.getUpdates(requestBody);
            default:
                throw new RuntimeException("unknown uri:" + uri);
        }
    }

    /**
     * Create base request body
     *
     * @return request object
     */
    private JsonObject createBaseRequestBody() {
        return updateRequestBody(new JsonObject());
    }


    /**
     * Create base request body
     *
     * @return request object
     */
    private JsonObject updateRequestBody(JsonObject requestBody) {
        long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TIMESTAMP, timestamp);
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_PUBLIC_KEY, PUBLIC_KEY);
        requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_SIGNATURE, mKey.signMessage(Long.toString(timestamp)));
        return requestBody;
    }

    /**
     * Execute request
     *
     * @param call
     * @return data object
     * @throws IOException
     */
    private JsonObject execute(Call<JsonObject> call) throws TeambrellaException {
        Response<JsonObject> response;

        try {
            response = call.execute();
        } catch (IOException e) {
            throw new TeambrellaClientException("Unable to execute request", e);
        }

        JsonObject responseBody = response.body();
        if (checkStatus(responseBody)) {
            JsonElement dataElement = responseBody.get(TeambrellaModel.ATTR_DATA);
            return dataElement.isJsonNull() ? null : dataElement.getAsJsonObject();
        }
        return null;
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
