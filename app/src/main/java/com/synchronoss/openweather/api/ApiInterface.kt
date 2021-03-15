package com.synchronoss.openweather.api
import com.synchronoss.openweather.model.WeatherResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    /*  @GET("data/2.5/weather?")
      fun getCurrentWeatherData(
          @Query("lat") lat: String?,
          @Query("lon") lon: String?,
          @Query("APPID") app_id: String?
      ): Call<WeatherResponse?>?*/

    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("APPID") app_id: String?
    ): Single<WeatherResponse>

}