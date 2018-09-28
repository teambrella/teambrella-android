package com.teambrella.android.ui.registration.join

import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.teambrella.android.BuildConfig
import com.teambrella.android.api.invite
import com.teambrella.android.api.teamId
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface JoinAPI {
    @POST("join/getWelcome")
    fun getWelcome(@Body request: JsonElement): Observable<Response<JsonObject>>
}

class JoinServer {

    private val api: JoinAPI = Retrofit.Builder()
            .baseUrl(Uri.Builder().scheme(BuildConfig.SCHEME).authority(BuildConfig.AUTHORITY).build().toString())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }).build()).build().create(JoinAPI::class.java)


    fun getWelcomeScreen(teamId: Int, invitationCode: String?, onSuccess: (JsonObject?) -> Unit, onError: () -> Unit) {
        val requestData = JsonObject().apply {
            this.teamId = teamId
            this.invite = invitationCode
        }


        var disposable: Disposable? = null

        fun onResponse(response: Response<JsonObject>) {
            onSuccess.invoke(response.body())
            disposable?.takeIf { !it.isDisposed }?.dispose()
        }

        fun onError(error: Throwable) {
            onError.invoke()
            disposable?.takeIf { !it.isDisposed }?.dispose()
        }

        disposable = api.getWelcome(requestData).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onResponse, ::onError)
    }


}