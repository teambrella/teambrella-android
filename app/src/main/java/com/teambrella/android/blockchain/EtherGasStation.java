package com.teambrella.android.blockchain;


import com.teambrella.android.util.log.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teambrella.android.BuildConfig;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class EtherGasStation {

    private static final String LOG_TAG = EtherGasStation.class.getSimpleName();

    private final List<EtherAPI> mGasAPIs = new LinkedList<>();
    private static final String[] TEST_AUTHORITIES = new String[]{BuildConfig.BASE_URL.replace("https://", "http://ropsten.")};
    private static final String[] MAIN_AUTHORITIES = new String[]{BuildConfig.BASE_URL};

    public EtherGasStation(boolean testNet) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String authorities[] = testNet ? TEST_AUTHORITIES : MAIN_AUTHORITIES;
        for (String authority : authorities) {
            mGasAPIs.add(new Retrofit.Builder()
                    .baseUrl(authority)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build().create(EtherAPI.class));
        }
    }


    public long checkGasPrice() {
        Long price = null;
        for (EtherAPI api : mGasAPIs) {
            try {
                    Response<Long> response = api.checkGasPrice().execute();

                    if (response.isSuccessful()) {
                        price = response.body();
                    }
            } catch (IOException e) {
                logCrash("Failed to check Gas Price.", e);
            }
            if (price != null) {
                break;
            }
        }
        return price == null ? -1 : price;
    }

    public long checkContractCreationGasPrice() {
        Long price = null;
        for (EtherAPI api : mGasAPIs) {
            try {
                Response<Long> response = api.checkContractCreationGasPrice().execute();

                if (response.isSuccessful()) {
                    price = response.body();
                }
            } catch (IOException e) {
                logCrash("Failed to check Contract Creation Gas Price.", e);
            }
            if (price != null) {
                break;
            }
        }
        return price == null ? -1 : price;
    }

    private void logCrash(String msg, Exception e){
        Log.e(LOG_TAG, msg, e);
        if (!BuildConfig.DEBUG){
            Crashlytics.logException(e);
        }
    }
}
