package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Hava tahmini servisinin menüsü.
 * Örnek istek:
 * `GET https://api.open-meteo.com/v1/forecast?latitude=39.92&longitude=32.85&current=...&timezone=auto`
 */
interface ForecastApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse

    companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,wind_speed_10m"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
        const val FORECAST_DAYS = 7
    }
}
