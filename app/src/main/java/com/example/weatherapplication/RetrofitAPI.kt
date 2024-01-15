package com.example.weatherapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitAPI {

    @GET("weather")
    fun getWeatherData(
        @Query("q") city: String?,
        @Query ("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Query ("appid") apiKey: String
    ) : Call<WeatherData>
}