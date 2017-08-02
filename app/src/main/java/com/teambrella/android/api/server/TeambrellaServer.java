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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

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

    private static final String SHARED_PREFS_NAME = "teambrella_api";
    private static final String TIMESTAMP_KEY = "timestamp";
    public static final String AUTHORITY = "surilla.com";
    public static final String BASE_URL = "https://surilla.com";
    public static final String SCHEME = "https";


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
            case TeambrellaUris.TEAMMATES_LIST:
                int teamId = TeambrellaUris.getTeamId(uri);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEAM_ID, teamId);
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_OFFSET, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_OFFSET)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_LIMIT, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_LIMIT)));
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
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_TEXT, uri.getQueryParameter(TeambrellaUris.KEY_TEXT));
                break;
            case TeambrellaUris.SET_CLAIM_VOTE:
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_CLAIM_ID, Integer.parseInt(uri.getQueryParameter(TeambrellaUris.KEY_ID)));
                requestBody.addProperty(TeambrellaModel.ATTR_REQUEST_MY_VOTE, Float.parseFloat(uri.getQueryParameter(TeambrellaUris.KEY_VOTE)) / 100);
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
            case TeambrellaUris.ME_UPDATES:
            case TeambrellaUris.ME_REGISTER_KEY:
            case TeambrellaUris.MY_TEAMS:
            case TeambrellaUris.NEW_FILE:
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
            case TeambrellaUris.ME_REGISTER_KEY:
                String facebookToken = uri.getQueryParameter(TeambrellaUris.KEY_FACEBOOK_TOKEN);
                return mAPI.registerKey(facebookToken);
            case TeambrellaUris.CLAIMS_LIST:
                return mAPI.getClaimsList(requestBody);
            case TeambrellaUris.CLAIMS_ONE:
                return mAPI.getClaim(requestBody);
            case TeambrellaUris.CLAIMS_CHAT:
                return mAPI.getClaimChat(requestBody);
            case TeambrellaUris.NEW_POST:
                return mAPI.newPost(requestBody);
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
                return mAPI.newFile(RequestBody.create(MediaType.parse("image/jpeg"), new File(uri.getQueryParameter(TeambrellaUris.KEY_URI))));
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


    public TeambrellaSocketClient createSocketClient(URI uri, int teamId, SocketClientListener listener) {
        Long timestamp = mPreferences.getLong(TIMESTAMP_KEY, 0L);
        String publicKey = mKey.getPublicKeyAsHex();
        String signature = mKey.signMessage(Long.toString(timestamp));
        HashMap<String, String> headers = new HashMap<>();
        headers.put("t", Long.toString(timestamp));
        headers.put("key", publicKey);
        headers.put("sig", signature);
        return new TeambrellaSocketClient(uri, teamId, headers, listener);
    }


    public interface SocketClientListener {

        void onMessage(String message);

        void onClose(int code, String reason, boolean remote);

        void onError(Exception ex);
    }

    public static class TeambrellaSocketClient extends WebSocketClient {


        private final int mTeamId;
        private final SocketClientListener mListener;


        TeambrellaSocketClient(URI serverUri, int mTeamId, Map<String, String> httpHeaders, SocketClientListener listener) {
            super(serverUri, new Draft_6455(), httpHeaders, 0);
            this.mTeamId = mTeamId;
            this.mListener = listener;
        }


        @Override
        public void connect() {
            try {
                setSocket(SSLSocketFactory.getDefault().createSocket());
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.connect();
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            send("0;" + mTeamId + ";1");
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
