package com.kampplus.hava.data

import com.google.gson.annotations.SerializedName
import com.kampplus.hava.model.MapCoordinate
import com.kampplus.hava.model.MapForecast
import com.kampplus.hava.model.MapWeatherDay
import com.kampplus.hava.model.MapWeatherMarker
import com.kampplus.hava.model.MapWeatherNow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MapWeatherApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = MAP_CURRENT_FIELDS,
        @Query("hourly") hourly: String = "relative_humidity_2m,pressure_msl",
        @Query("daily") daily: String = "weather_code,temperature_2m_min,temperature_2m_max,wind_speed_10m_max",
        @Query("forecast_days") days: Int = 5,
        @Query("timezone") timezone: String = "auto"
    ): MapWeatherResponse

    @GET("v1/forecast")
    suspend fun markers(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("current") current: String = MAP_CURRENT_FIELDS,
        @Query("forecast_days") days: Int = 1,
        @Query("timezone") timezone: String = "GMT"
    ): List<MapWeatherResponse>
}

const val MAP_CURRENT_FIELDS = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,pressure_msl,precipitation"

data class MapWeatherResponse(
    val timezone: String? = null,
    val current: MapCurrentResponse? = null,
    val hourly: MapHourlyResponse? = null,
    val daily: MapDailyResponse? = null
)
data class MapCurrentResponse(
    val time: String? = null,
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("relative_humidity_2m") val humidity: Double? = null,
    @SerializedName("weather_code") val code: Int? = null,
    @SerializedName("wind_speed_10m") val wind: Double? = null,
    @SerializedName("pressure_msl") val pressure: Double? = null,
    val precipitation: Double? = null
)
data class MapHourlyResponse(
    val time: List<String>? = null,
    @SerializedName("relative_humidity_2m") val humidity: List<Double?>? = null,
    @SerializedName("pressure_msl") val pressure: List<Double?>? = null
)
data class MapDailyResponse(
    val time: List<String>? = null,
    @SerializedName("weather_code") val code: List<Int?>? = null,
    @SerializedName("temperature_2m_min") val minimum: List<Double?>? = null,
    @SerializedName("temperature_2m_max") val maximum: List<Double?>? = null,
    @SerializedName("wind_speed_10m_max") val wind: List<Double?>? = null
)

fun MapCurrentResponse.toMapNow() = MapWeatherNow(
    time.orEmpty(), temperature, code, humidity, wind, pressure, precipitation
)

fun MapWeatherResponse.toMapForecast(): MapForecast {
    val current = requireNotNull(current) { "Bu konum için güncel hava verisi bulunamadı." }
    val days = daily?.time.orEmpty().take(5).mapIndexed { index, date ->
        val hours = hourly?.time.orEmpty().indices.filter { hourly?.time?.get(it)?.startsWith(date) == true }
        fun mean(values: List<Double?>?): Double? = hours.mapNotNull { values?.getOrNull(it) }
            .takeIf { it.isNotEmpty() }?.average()
        MapWeatherDay(date, daily?.minimum?.getOrNull(index), daily?.maximum?.getOrNull(index),
            daily?.code?.getOrNull(index), mean(hourly?.humidity), daily?.wind?.getOrNull(index), mean(hourly?.pressure))
    }
    return MapForecast(current.toMapNow(), days, timezone ?: "UTC")
}

class MapWeatherRepository(private val api: MapWeatherApi = mapWeatherApi) {
    private val cache = LinkedHashMap<String, Pair<Long, MapForecast>>()
    suspend fun forecast(point: MapCoordinate): MapForecast {
        val key = "${point.latitude},${point.longitude}"
        cache[key]?.takeIf { System.currentTimeMillis() - it.first < 600_000 }?.let { return it.second }
        return api.forecast(point.latitude, point.longitude).toMapForecast().also {
            cache[key] = System.currentTimeMillis() to it
            if (cache.size > 40) cache.remove(cache.keys.first())
        }
    }
    suspend fun markers(points: List<MapCoordinate>): List<MapWeatherMarker> {
        require(points.size in 2..6)
        val response = api.markers(points.joinToString(",") { it.latitude.toString() }, points.joinToString(",") { it.longitude.toString() })
        return points.mapIndexedNotNull { i, point -> response.getOrNull(i)?.current?.let { MapWeatherMarker(point, it.toMapNow()) } }
    }
}

private val mapWeatherApi: MapWeatherApi by lazy {
    Retrofit.Builder().baseUrl("https://api.open-meteo.com/")
        .client(OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(MapWeatherApi::class.java)
}
