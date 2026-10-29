package com.kampplus.hava.model

import java.util.Locale
import kotlin.math.roundToInt

/** Conservative surface observation decoder. Missing measurements remain unknown. */
object MetarDecoderEngine {
    private val windRegex = Regex("\\b(\\d{3}|VRB)(\\d{2,3})(?:G(\\d{2,3}))?(KT|MPS)\\b")
    private val cloudRegex = Regex("\\b(FEW|SCT|BKN|OVC|VV)(\\d{3}|///)(CB|TCU)?\\b")

    fun decode(rawMetar: String): DecodedMetar {
        val raw = rawMetar.trim()
        // Trends are forecasts, not part of the observed flight category.
        val observation = raw.split(Regex("\\b(?:TEMPO|BECMG|NOSIG|RMK)\\b"), 2)[0]
        val cavok = Regex("\\bCAVOK\\b").containsMatchIn(observation)
        val wind = windRegex.find(observation)?.let {
            val multiplier = if (it.groupValues[4] == "MPS") 1.943844 else 1.0
            WindCondition(it.groupValues[1], (it.groupValues[2].toInt() * multiplier).roundToInt(), it.groupValues[3].toIntOrNull()?.let { gust -> (gust * multiplier).roundToInt() })
        } ?: WindCondition()
        val visibility = visibilityMiles(observation, cavok)
        val clouds = cloudRegex.findAll(observation).toList()
        val ceilingClouds = clouds.filter { it.groupValues[1] in listOf("BKN", "OVC", "VV") }
        val ceiling = ceilingClouds.mapNotNull { it.groupValues[2].toIntOrNull()?.times(100) }.minOrNull()
        val skyKnown = cavok || clouds.isNotEmpty() || Regex("\\b(SKC|CLR|NSC|NCD)\\b").containsMatchIn(observation)
        val unknownCeiling = ceilingClouds.any { it.groupValues[2] == "///" }
        val category = when {
            visibility != null && visibility < 1 || ceiling != null && ceiling < 500 -> FlightCategory.LIFR
            visibility == null || !skyKnown || unknownCeiling -> FlightCategory.UNKNOWN
            visibility < 3 || ceiling != null && ceiling < 1000 -> FlightCategory.IFR
            visibility <= 5 || ceiling != null && ceiling <= 3000 -> FlightCategory.MVFR
            else -> FlightCategory.VFR
        }
        val temp = Regex("\\b(M?\\d{2}|//)/(M?\\d{2}|//)(?=\\s|$)").find(observation)
        fun temperature(index: Int) = temp?.groupValues?.get(index)?.replace("M", "-")?.toIntOrNull()
        val qnh = Regex("\\bQ(\\d{4})\\b").find(observation)?.groupValues?.get(1)?.let { "$it hPa" }
            ?: Regex("\\bA(\\d{4})\\b").find(observation)?.groupValues?.get(1)?.toInt()?.let { "${(it / 100.0 * 33.86389).roundToInt()} hPa (${it / 100.0} inHg)" }
        val station = Regex("^(?:(?:METAR|SPECI)\\s+)?(?:COR\\s+)?([A-Z][A-Z0-9]{3})\\b").find(raw)?.groupValues?.get(1).orEmpty()
        val weatherTokens = observation.split(Regex("\\s+")).filter { Regex("[-+]?(?:VC)?(?:MI|PR|BC|DR|BL|SH|TS|FZ)?(?:DZ|RA|SN|SG|IC|PL|GR|GS|UP|BR|FG|FU|VA|DU|SA|HZ|PO|SQ|FC|SS|DS)+").matches(it) }
        return DecodedMetar(raw, station, wind,
            when { cavok -> "≥ 10 km · CAVOK"; Regex("\\b9999\\b").containsMatchIn(observation) -> "≥ 10 km"; visibility != null -> String.format(Locale.ROOT, "%.1f km", visibility * 1.609344); else -> "Bildirilmedi" },
            qnh ?: "Bildirilmedi", temperature(1), temperature(2), category, cavok, ceiling,
            when { cavok -> "CAVOK · önemli bulut yok"; clouds.isNotEmpty() -> clouds.joinToString(" · ") { "${it.groupValues[1]} ${it.groupValues[2].toIntOrNull()?.times(100)?.let { height -> "$height ft" } ?: "yükseklik belirsiz"}${it.groupValues[3]}" }; skyKnown -> "Önemli bulut bildirilmedi"; else -> "Bildirilmedi" },
            Regex("\\b(\\d{2})(\\d{2})(\\d{2})Z\\b").find(observation)?.let { "${it.groupValues[1]}. gün · ${it.groupValues[2]}:${it.groupValues[3]} UTC" }.orEmpty(),
            if (cavok) "CAVOK" else weatherTokens.joinToString(" · ").ifBlank { "Önemli hadise bildirilmedi" })
    }

    private fun visibilityMiles(raw: String, cavok: Boolean): Double? {
        if (cavok) return 10 / 1.609344
        val sm = Regex("(?<!\\S)([PM]?)(?:(\\d+) )?(\\d+)(?:/(\\d+))?SM\\b").find(raw)
        if (sm != null) {
            val whole = sm.groupValues[2].toDoubleOrNull() ?: 0.0
            val numerator = sm.groupValues[3].toDouble()
            val denominator = sm.groupValues[4].toDoubleOrNull() ?: 1.0
            if (denominator == 0.0) return null
            val miles = whole + numerator / denominator
            return when (sm.groupValues[1]) { "M" -> miles - 0.001; "P" -> miles + 0.001; else -> miles }
        }
        return Regex("(?<!\\S)(\\d{4})(?:NDV)?(?=\\s|$)").find(raw)?.groupValues?.get(1)?.toDouble()?.div(1609.344)
    }
}

/** TAF groups are displayed independently: omitted fields are not invented or inherited across TEMPO/PROB. */
object TafDecoderEngine {
    fun decode(raw: String): List<TafPeriod> {
        if (raw.isBlank()) return emptyList()
        val normalized = raw.replace(Regex("\\s+"), " ").trim()
        val markers = Regex("\\b(?:FM\\d{6}|BECMG|TEMPO|PROB(?:30|40)(?: TEMPO)?)\\b").findAll(normalized).toList()
        val starts = listOf(0) + markers.map { it.range.first }
        return starts.mapIndexed { index, start ->
            val group = normalized.substring(start, starts.getOrNull(index + 1) ?: normalized.length).trim()
            val validity = Regex("\\b\\d{4}/\\d{4}\\b").find(group)?.value
            val title = if (index == 0) "Ana tahmin" else when {
                group.startsWith("FM") -> "Başlangıç ${group.take(8)} UTC"
                group.startsWith("BECMG") -> "Kademeli değişim"
                group.startsWith("TEMPO") -> "Geçici koşullar"
                else -> "Olasılık ${group.takeWhile { it != ' ' }.removePrefix("PROB")}%"
            }
            TafPeriod(title + validity?.let { " · $it UTC" }.orEmpty(), group, MetarDecoderEngine.decode(group))
        }
    }
}
