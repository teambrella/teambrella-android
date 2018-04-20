package com.teambrella.android.blockchain;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Blockchain API
 */
public interface BlockchainAPI {

    @GET("api/addr/{address}/balance")
    Call<String> checkBalance(@Path("address") String address);

    @GET("api/addr/{address}/utxo")
    Call<List<ExplorerUtxo>> fetchUtxos(@Path("address") String address);

    @POST("api/tx/send")
    Call<ExplorerTxResult> pushTransaction(@Body JsonObject body);

    @GET("api/tx/{txid}")
    Call<JsonObject> checkTx(@Path("txid") String txid);


}
