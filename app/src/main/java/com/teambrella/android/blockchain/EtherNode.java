package com.teambrella.android.blockchain;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class EtherNode {

    private static final String LOG_TAG = EtherNode.class.getSimpleName();

    private final List<EtherAPI> mEtherAPIs = new LinkedList<>();
    private final boolean mIsTestNet;
    private static final String[] TEST_AUTHORITIES = new String[]{"https://ropsten.etherscan.io"};
    private static final String[] MAIN_AUTHORITIES = new String[]{"http://api.etherscan.io"};

    public EtherNode(boolean testNet) {
        this.mIsTestNet = testNet;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String authorities[] = mIsTestNet ? TEST_AUTHORITIES : MAIN_AUTHORITIES;
        for (String authority : authorities) {
            mEtherAPIs.add(new Retrofit.Builder()
                    .baseUrl(authority)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build().create(EtherAPI.class));
        }
    }


    public Scan<TxReceiptResult> checkTx(String creationTx) {
        Scan<TxReceiptResult> receipt = null;
        for (EtherAPI api : mEtherAPIs) {
            try {
                    Response<Scan<TxReceiptResult>> response = api.checkTx(creationTx).execute();
                    Thread.currentThread().sleep(1000);
                    if (response.isSuccessful()) {
                        receipt = response.body();
                    }
            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "Failed to check Tx.", e);
            }
            if (receipt != null) {
                break;
            }
        }
        return receipt;
    }

    public long checkNonce(String addressHex) {
        Scan<String> responceBody = null;
        for (EtherAPI api : mEtherAPIs) {
            try {
                Response<Scan<String>> response = api.checkNonce(addressHex).execute();
                Thread.currentThread().sleep(1000);
                if (response.isSuccessful()) {
                    responceBody = response.body();

                    String hex = responceBody.result;
                    if (hex.startsWith("0x"))
                        hex = hex.substring(2);

                    return Long.parseLong(hex, 16);
                }
            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "Failed to check Nonce:" + e.getMessage(), e);
            }
        }

        return 0;
    }

    public long checkBalance(String addressHex) {
        Scan<Long> responceBody = null;
        for (EtherAPI api : mEtherAPIs) {
            try {
                Response<Scan<Long>> response = api.checkBalance(addressHex).execute();
                Thread.currentThread().sleep(1000);
                if (response.isSuccessful()) {
                    responceBody = response.body();

                    Long balance = responceBody.result;
                    if (null == balance)
                        return -1;

                    return balance.longValue();
                }
            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "Failed to check balance: " + e.getMessage(), e);
            }
        }

        return 0;
    }

    public String pushTx(String hex) {
        JsonObject result = null;
        for (EtherAPI api : mEtherAPIs) {
            try {
                Response<JsonObject> response = api.pushTx(hex).execute();
                if (response.isSuccessful()) {
                    result = response.body();
                    //        {
                    //            "jsonrpc": "2.0",
                    //                "error": {
                    //            "code": -32010,
                    //                    "message": "Transaction nonce is too low. Try incrementing the nonce.",
                    //                    "data": null
                    //        },
                    //            "id": 1
                    //        {
                    //              "jsonrpc": "2.0",
                    //              "result": "0x918a3313e6c1c5a0068b5234951c916aa64a8074fdbce0fecbb5c9797f7332f6",
                    //              "id": 1
                    //          }

                    JsonElement r = result.get("result");
                    if (r != null)
                        return r.getAsString();
                    else
                        Log.e(LOG_TAG, "Could not publish eth multisig creation tx. The answer was: " + result.toString());

                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return null;
    }
}
