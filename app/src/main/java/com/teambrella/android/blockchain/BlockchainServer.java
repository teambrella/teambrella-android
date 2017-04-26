package com.teambrella.android.blockchain;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class BlockchainServer {

    private static final String LOG_TAG = BlockchainServer.class.getSimpleName();

    private final List<BlockchainAPI> mBlockchainAPIs = new LinkedList<>();
    private final boolean mIsTestNet;
    private static final String[] TEST_AUTHORITIES = new String[]{"https://test-insight.bitpay.com", "https://testnet.blockexplorer.com"};
    private static final String[] MAIN_AUTHORITIES = new String[]{"https://insight.bitpay.com", "https://blockexplorer.com", "https://blockchain.info"};

    public BlockchainServer(boolean testNet) {
        this.mIsTestNet = testNet;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String authorities[] = mIsTestNet ? TEST_AUTHORITIES : MAIN_AUTHORITIES;
        for (String authority : authorities) {
            mBlockchainAPIs.add(new Retrofit.Builder()
                    .baseUrl(authority)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build().create(BlockchainAPI.class));
        }
    }


    public List<ExplorerUtxo> fetchUtxo(String address) {
        List<ExplorerUtxo> list = null;
        for (BlockchainAPI api : mBlockchainAPIs) {
            try {
                list = api.fetchUtxos(address).execute().body();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
            if (list != null) {
                break;
            }
        }
        return list;
    }

    public boolean pushTransaction(String transaction) {
        ExplorerTxResult result = null;
        JsonObject body = new JsonObject();
        body.add("rawtx", new JsonPrimitive(transaction));
        for (BlockchainAPI api : mBlockchainAPIs) {
            try {
                Response<ExplorerTxResult> response = api.pushTransaction(body).execute();
                if (response.isSuccessful()) {
                    result = response.body();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
            if (result != null) {
                break;
            }
        }


        return result != null;
    }
}
