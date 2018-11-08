package com.elpassion.weather

import com.elpassion.weather.data.ApiService
import com.elpassion.weather.data.Repository
import com.squareup.moshi.Moshi
import org.junit.Ignore
import org.junit.Test

@Ignore
class ApiServiceIntegrationTests {

    val appid = Repository.appid

    // Moshi adapters just for logging..
    val moshi = Moshi.Builder().build()!!
    val moshiForecastAdapter = moshi.adapter(ApiService.Forecast::class.java)!!
    val moshiForecastsAdapter = moshi.adapter(ApiService.Forecasts::class.java)!!
    val moshiDailyForecastsAdapter = moshi.adapter(ApiService.DailyForecasts::class.java)!!

    fun log(msg: Any?) = println(msg)
    fun log(forecast: ApiService.Forecast?) = log(moshiForecastAdapter.toJson(forecast))
    fun log(forecasts: ApiService.Forecasts?) = log(moshiForecastsAdapter.toJson(forecasts))
    fun log(dailyForecasts: ApiService.DailyForecasts?) = log(moshiDailyForecastsAdapter.toJson(dailyForecasts))

    val cities = listOf("Wroclaw", "Warsaw", "London", "New York")

    @Test
    fun logForecastForGivenCities() {
        for (city in cities)
            log(getForecastByCity(city))
    }

    @Test
    fun logForecastsForGivenCities() {
        for (city in cities)
            log(getForecastsByCity(city))
    }

    @Test
    fun logDailyForecastsForGivenCities() {
        for (city in cities)
            log(getDailyForecastsByCity(city))
    }

    fun getForecastByCity(city: String): ApiService.Forecast? {
        val call = ApiService.service.getForecastByCity(appid, city, "metric")
        val response = call.execute()
        val body = response.body()
        return body
    }

    fun getForecastsByCity(city: String): ApiService.Forecasts? {
        val call = ApiService.service.getForecastsByCity(appid, city, 32, "metric")
        val response = call.execute()
        val body = response.body()
        return body
    }

    fun getDailyForecastsByCity(city: String): ApiService.DailyForecasts? {
        val call = ApiService.service.getDailyForecastsByCity(appid, city, 16, "metric")
        val response = call.execute()
        val body = response.body()
        return body
    }
}