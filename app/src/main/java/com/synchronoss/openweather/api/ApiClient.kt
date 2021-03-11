package com.synchronoss.openweather.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private const val serviceTimeOut: Long = 30000
    @JvmStatic
    fun getClient(baseUrl: String?): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(
                            serviceTimeOut,
                            TimeUnit.MILLISECONDS
                        )
                        .writeTimeout(
                            serviceTimeOut,
                            TimeUnit.MILLISECONDS
                        )
                        .readTimeout(
                            serviceTimeOut,
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                )
                .build()
        }
        return retrofit
    }
}