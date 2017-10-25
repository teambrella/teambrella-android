package com.teambrella.android.blockchain;

import com.google.gson.JsonObject;

import java.math.BigInteger;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EtherAPI {
    //https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=0xf904808000831cfde080&apikey=YourApiKeyToken
    @FormUrlEncoded
    @POST("api?module=proxy&action=eth_sendRawTransaction")
    Call<JsonObject> pushTx(@Field("hex") String hex);

    @GET("api?module=proxy&action=eth_getTransactionCount")
    Call<Scan<String>> checkNonce(@Query("address") String address);

    @GET("api?module=proxy&action=eth_getTransactionReceipt")
    Call<Scan<ScanResultTxReceipt>> checkTx(@Query("txhash") String txHash);

    @GET("api?module=proxy&action=eth_call")
    Call<Scan<String>> readContractString(@Query("to") String to, @Query("data") String callData);

    @GET("api?module=account&action=balance")
    Call<Scan<BigInteger>> checkBalance(@Query("address") String address);

    @GET("api/eth_gasPrice")
    Call<Long> checkGasPrice();

    @GET("api/eth_gasPrice/ContractCreation")
    Call<Long> checkContractCreationGasPrice();
}
