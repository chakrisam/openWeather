package com.synchronoss.openweather.api

import com.synchronoss.openweather.api.ApiClient.getClient

object ApiUtils {
    private const val BASE_URL = "http://api.openweathermap.org/"
    val apiService: ApiService
        get() = getClient(BASE_URL)!!.create(ApiService::class.java)
}