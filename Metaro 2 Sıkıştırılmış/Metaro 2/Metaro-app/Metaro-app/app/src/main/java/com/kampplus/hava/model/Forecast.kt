package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime
data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

data class CurrentWeather(
    val temperatureC: Double,
    val weatherCode: Int,
    val observedAt: LocalDateTime,
    val apparentTemperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null
)

/** Tek bir saatlik tahmin satırı. */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null
)

/** Tek bir günlük tahmin satırı: o günün en düşük ve en yüksek sıcaklığı. */
data class DailyForecast(
    val date: LocalDate,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null
)
