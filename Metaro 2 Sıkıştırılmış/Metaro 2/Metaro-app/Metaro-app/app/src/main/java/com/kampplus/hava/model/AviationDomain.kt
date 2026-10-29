package com.kampplus.hava.model

enum class FlightCategory { VFR, MVFR, IFR, LIFR, UNKNOWN }

data class AirportNode(val icao: String, val name: String, val latitude: Double, val longitude: Double)

data class WindCondition(
    val directionDegrees: String = "—",
    val speedKnots: Int? = null,
    val gustKnots: Int? = null
) {
    val isCrosswindRisk: Boolean get() = (speedKnots ?: 0) > 15 || (gustKnots ?: 0) > 20
    val display: String get() = speedKnots?.let { "$directionDegrees${if (directionDegrees == "VRB") "" else "°"} / $it kt${gustKnots?.let { gust -> " · hamle $gust kt" }.orEmpty()}" } ?: "Bildirilmedi"
}

data class DecodedMetar(
    val rawText: String,
    val stationIcao: String = "",
    val wind: WindCondition = WindCondition(),
    val visibilityKm: String = "Bildirilmedi",
    val altimeterQnh: String = "Bildirilmedi",
    val temperatureC: Int? = null,
    val dewPointC: Int? = null,
    val flightCategory: FlightCategory = FlightCategory.UNKNOWN,
    val isCavok: Boolean = false,
    val ceilingFeet: Int? = null,
    val clouds: String = "Bildirilmedi",
    val observationTime: String = "",
    val weather: String = "Bildirilmedi"
)

data class TafPeriod(val title: String, val raw: String, val conditions: DecodedMetar)
