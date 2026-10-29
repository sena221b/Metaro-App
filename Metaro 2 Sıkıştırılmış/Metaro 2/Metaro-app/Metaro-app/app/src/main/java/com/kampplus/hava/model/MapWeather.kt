package com.kampplus.hava.model

/** Forecast values are model estimates, never aviation observations. Missing values stay missing. */
data class MapWeatherNow(
    val time: String,
    val temperature: Double?,
    val code: Int?,
    val humidity: Double?,
    val wind: Double?,
    val pressure: Double?,
    val precipitation: Double?
)

data class MapWeatherDay(
    val date: String,
    val minimum: Double?,
    val maximum: Double?,
    val code: Int?,
    val humidityMean: Double?,
    val windMaximum: Double?,
    val pressureMean: Double?
)

data class MapForecast(val now: MapWeatherNow, val days: List<MapWeatherDay>, val timezone: String)
data class MapCoordinate(val latitude: Double, val longitude: Double)
data class MapWeatherMarker(val point: MapCoordinate, val weather: MapWeatherNow)

fun destinationAdvice(code: String, weather: MapWeatherNow): String {
    val wet = (weather.precipitation ?: 0.0) > 0 || weather.code in 51..99
    val indoor = wet || (weather.wind ?: 0.0) >= 40 || (weather.temperature ?: 20.0) <= 5
    val place = when (code) {
        "EGLL", "EGLC", "EGKK" -> if (indoor) "Londra'da British Museum" else "Londra'da Hyde Park"
        "LTFM", "LTFJ" -> if (indoor) "İstanbul'da İstanbul Hava Kuvvetleri Müzesi" else "İstanbul'da Sarıyer Kireçburnu Sahili"
        "LFPG", "LFPO" -> if (indoor) "Paris'te Musée d'Orsay" else "Paris'te Jardin du Luxembourg"
        "KJFK", "KLGA", "KEWR" -> if (indoor) "New York'ta Metropolitan Museum" else "New York'ta Central Park"
        "RJTT", "RJAA" -> if (indoor) "Tokyo'da Tokyo National Museum" else "Tokyo'da Ueno Park"
        else -> null
    }
    val weatherTip = when {
        wet -> "Şemsiyenizi almak isteyebilirsiniz."
        (weather.wind ?: 0.0) >= 40 -> "Rüzgâra uygun giyinemenizi tavsiye ederiz; kapalı alanları tercih edebilirsiniz."
        (weather.temperature ?: 20.0) <= 5 -> "Kalın bir katman almayı unutmayın."
        (weather.temperature ?: 20.0) >= 30 -> "Su ve güneş koruyucu alın; öğlen sıcağında mola verin."
        else -> "Rahat ayakkabılarla kısa bir keşif yürüyüşü planlayabilirsiniz."
    }
    val activity = place?.let { "$it ziyaretini düşünebilirsiniz; ulaşım ve ziyaret saatlerini kontrol edin." }
        ?: if (indoor) "$code çevresinde bir müze veya kafede mola verin; kahvenize Küçük Prens eşlik edebilir."
        else "$code çevresindeki bir parkı veya şehir merkezini keşfedip yerel bir kafede mola verin."
    return "$weatherTip $activity"
}
