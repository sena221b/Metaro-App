package com.kampplus.hava.data

import com.kampplus.hava.model.AirportNode
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit

interface AviationApi {
    @GET("api/data/metar")
    suspend fun getMetar(@Query("ids") icaoCode: String, @Query("format") format: String = "raw"): Response<String>
    @GET("api/data/taf")
    suspend fun getTaf(@Query("ids") icaoCode: String, @Query("format") format: String = "raw"): Response<String>
    @GET("api/data/airport")
    suspend fun airports(@Query("ids") code: String, @Query("format") format: String = "json"): Response<List<AirportDto>>
}

data class AirportDto(val icaoId: String?, val iataId: String?, val name: String?, val lat: Double?, val lon: Double?)

object AviationApiClient {
    val instance: AviationApi by lazy {
        Retrofit.Builder().baseUrl("https://aviationweather.gov/")
            .client(OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(25, TimeUnit.SECONDS)
                .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("User-Agent", "Metaro-Android/1.0 (weather briefing)").build()) }.build())
            .addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create())
            .build().create(AviationApi::class.java)
    }
}

class AirportRepository(private val api: AviationApi = AviationApiClient.instance) {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, AirportNode>()
    suspend fun resolve(code: String): AirportNode {
        val normalized = code.trim().uppercase(Locale.ROOT)
        require(Regex("[A-Z0-9]{3,4}").matches(normalized)) { "IATA veya ICAO kodu girin." }
        cache[normalized]?.let { return it }
        val response = api.airports(normalized)
        if (!response.isSuccessful) throw java.io.IOException("Havalimanı servisi: HTTP ${response.code()}")
        val dto = response.body()?.firstOrNull { it.icaoId == normalized || it.iataId == normalized }
            ?: throw IllegalArgumentException("$normalized için havalimanı bulunamadı.")
        val node = AirportNode(dto.icaoId ?: normalized, dto.name ?: normalized,
            dto.lat ?: throw IllegalArgumentException("Meydan koordinatı bulunamadı."),
            dto.lon ?: throw IllegalArgumentException("Meydan koordinatı bulunamadı."))
        cache[normalized] = node
        return node
    }
}
