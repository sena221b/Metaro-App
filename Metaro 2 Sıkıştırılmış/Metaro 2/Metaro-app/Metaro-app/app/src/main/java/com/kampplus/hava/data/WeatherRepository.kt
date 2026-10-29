package com.kampplus.hava.data

import com.kampplus.hava.model.Forecast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WeatherRepository {
    /** Verilen koordinat için anlık durum + saatlik + günlük tahmini getirir. */
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast
}

/** tahmini Open-Meteo forecast servisinden alır. */
class OpenMeteoWeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast = withContext(ioDispatcher) {

        api.getForecast(latitude = latitude, longitude = longitude).toForecast()
    }
}
