package com.teambrella.android.blockchain;


import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.util.log.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
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
    private static final String[] TEST_AUTHORITIES = new String[]{"https://ropsten.etherscan.io"};
    private static final String[] MAIN_AUTHORITIES = new String[]{"http://api.etherscan.io"};

    public EtherNode(boolean testNet) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //noinspection ConstantConditions
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String authorities[] = testNet ? TEST_AUTHORITIES : MAIN_AUTHORITIES;
        for (String authority : authorities) {
            mEtherAPIs.add(new Retrofit.Builder()
                    .baseUrl(authority)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build().create(EtherAPI.class));
        }
    }


    public Scan<ScanResultTxReceipt> checkTx(String creationTx) {
        Scan<ScanResultTxReceipt> receipt = null;
        for (EtherAPI api : mEtherAPIs) {
            try {
                pause();
                Response<Scan<ScanResultTxReceipt>> response = api.checkTx(creationTx).execute();
                if (response.isSuccessful()) {
                    receipt = response.body();
                }

                checkError(receipt);

            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "Failed to check Tx.", e);
                if (!BuildConfig.DEBUG) {
                    Crashlytics.logException(e);
                }
            }
            if (receipt != null) {
                break;
            }
        }
        return receipt;
    }

    public long checkNonce(String addressHex) {
        for (EtherAPI api : mEtherAPIs) {
            try {
                pause();
                Response<Scan<String>> response = api.checkNonce(addressHex).execute();
                if (response.isSuccessful()) {

                    Scan<String> responseBody = response.body();
                    String hex = responseBody != null ? responseBody.result : null;
                    if (hex != null && hex.startsWith("0x")) {
                        hex = hex.substring(2);
                    }

                    checkError(responseBody);

                    return hex != null ? Long.parseLong(hex, 16) : 0;
                }
            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "Failed to check Nonce:" + e.getMessage(), e);
                if (!BuildConfig.DEBUG) {
                    Crashlytics.logException(e);
                }
            }
        }

        return 0;
    }

    public BigDecimal checkBalance(String addressHex) {
        Scan<BigInteger> responseBody;
        for (EtherAPI api : mEtherAPIs) {
            try {
                pause();
                Response<Scan<BigInteger>> response = api.checkBalance(addressHex).execute();
                if (response.isSuccessful()) {
                    responseBody = response.body();

                    BigInteger balance = responseBody != null ? responseBody.result : null;

                    checkError(responseBody);

                    if (null == balance)
                        return new BigDecimal(-1);

                    //noinspection BigDecimalMethodWithoutRoundingCalled
                    return new BigDecimal(balance, MathContext.UNLIMITED).divide(AbiArguments.WEIS_IN_ETH);
                }
            } catch (IOException | InterruptedException | JsonSyntaxException | ArithmeticException e) {
                Log.e(LOG_TAG, "Failed to check balance: " + e.getMessage(), e);
                if (!BuildConfig.DEBUG) {
                    Crashlytics.logException(e);
                }
            }
        }

        return new BigDecimal(-1, MathContext.UNLIMITED);
    }

    public int readContractInt(String to, String callData) {

        for (EtherAPI api : mEtherAPIs) {
            try {
                pause();
                Response<Scan<String>> response = api.readContractString(to, callData).execute();

                checkError(response.body());

                return parseBigIntegerOrMinusOne(response).intValue();

            } catch (IOException | InterruptedException | JsonSyntaxException e) {
                Log.e(LOG_TAG, "Failed to check balance: " + e.getMessage(), e);
                if (!BuildConfig.DEBUG) {
                    Crashlytics.logException(e);
                }
            }
        }

        return -1;
    }

    public String pushTx(String hex) {
        JsonObject result;
        for (EtherAPI api : mEtherAPIs) {
            try {
                pause();
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
                    JsonElement r = result != null ? result.get("result") : null;
                    if (r != null) {
                        return r.getAsString();
                    } else {
                        Log.e(LOG_TAG, "Could not publish eth multisig creation tx. The answer was: " + (result != null ? result.toString() : null));
                        if (!BuildConfig.DEBUG) {
                            if (result != null) {
                                Crashlytics.logException(new EtherException(result.toString()));
                            }
                        }
                    }

                }
            } catch (IOException | InterruptedException e) {
                Log.e(LOG_TAG, "", e);
                if (!BuildConfig.DEBUG) {
                    Crashlytics.logException(e);
                }
            }
        }
        return null;
    }

    private BigInteger parseBigIntegerOrMinusOne(Response<Scan<String>> response) {
        if (response.isSuccessful()) {
            Scan<String> responseBody = response.body();
            String s = responseBody != null ? responseBody.result : null;
            if (s != null) {
                byte[] bytes = Hex.toBytes(s);
                return new BigInteger(bytes);
            }
        }
        return new BigInteger("-1");
    }


    private void pause() throws InterruptedException {
        //Thread.sleep(200);
    }

    @SuppressWarnings("ThrowableNotThrown")
    private <T> void checkError(Scan<T> response) {
        JsonObject error = response != null ? response.error : null;
        try {
            if (error != null) {
                throw new EtherException(error.toString());
            }
        } catch (EtherException e) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "", new EtherException(error.toString()));
            } else {
                Crashlytics.logException(new EtherException(error.toString()));
            }
        }
    }
}
