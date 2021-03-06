package com.teambrella.android.ui.registration.location

import android.content.Context
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.Filter
import com.google.gson.GsonBuilder
import com.teambrella.android.BuildConfig
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface TeambrellaCityAPI {
    @GET("GeoObject/GetCities")
    fun getCityList(@Query("q") query: String): Observable<List<String>>
}

class CityAdapter(context: Context) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {

    private val filter = CityFilter()


    override fun getFilter(): Filter {
        return filter
    }

    inner class CityFilter : Filter() {

        private val api: TeambrellaCityAPI = Retrofit.Builder()
                .baseUrl(Uri.Builder().scheme(BuildConfig.SCHEME).authority(BuildConfig.AUTHORITY).build().toString())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }).build()).build().create(TeambrellaCityAPI::class.java)


        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()
            constraint?.let {
                try {
                    val list = api.getCityList(it.toString()).blockingFirst()
                    result.values = list
                    result.count = list?.size ?: 0
                } catch (e: Exception) {
                }
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.let {
                clear()
                if (it.values is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    addAll(it.values as List<String>)
                    notifyDataSetChanged()
                }
                notifyDataSetChanged()
            }
        }
    }
}
