package com.kampplus.hava.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Bu sınıf UTC (Zulu) saatini biçimlendirmekten ve hesaplamaktan sorumludur.
 */
object ZuluTimeManager {

    private const val ZULU_FORMAT = "HH:mm 'Z'"
    private const val TIME_ZONE_UTC = "UTC"

    fun getCurrentZuluTime(): String {
        val formatter = SimpleDateFormat(ZULU_FORMAT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone(TIME_ZONE_UTC)
        }
        return formatter.format(Date())
    }
}
