package com.kampplus.hava.data

import com.kampplus.hava.model.MapWeatherNow
import com.kampplus.hava.model.destinationAdvice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapWeatherTest {
    @Test fun `daily averages use matching local date and ignore missing hours`() {
        val response = MapWeatherResponse(
            timezone = "Europe/Istanbul",
            current = MapCurrentResponse(time = "2026-10-28T12:00", temperature = 18.0),
            hourly = MapHourlyResponse(
                time = listOf("2026-10-28T23:00", "2026-10-29T00:00", "2026-10-29T01:00"),
                humidity = listOf(40.0, 60.0, 80.0), pressure = listOf(1000.0, null, 1010.0)
            ),
            daily = MapDailyResponse(time = listOf("2026-10-28", "2026-10-29", "2026-10-30"))
        ).toMapForecast()
        assertEquals(40.0, response.days[0].humidityMean!!, 0.001)
        assertEquals(70.0, response.days[1].humidityMean!!, 0.001)
        assertEquals(1010.0, response.days[1].pressureMean!!, 0.001)
        assertNull(response.days[2].humidityMean)
        assertNull(response.days[0].minimum)
        assertEquals("Europe/Istanbul", response.timezone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `missing current response cannot produce a fake forecast`() {
        MapWeatherResponse().toMapForecast()
    }

    @Test fun `recommendations change with destination and do not declare flight safety`() {
        val weather = MapWeatherNow("2026-10-28T12:00", 12.0, 61, 80.0, 12.0, 1010.0, 1.0)
        val london = destinationAdvice("EGLL", weather)
        val istanbul = destinationAdvice("LTFM", weather)
        assertTrue(london.contains("Şemsiye"))
        assertTrue(london.contains("British Museum"))
        assertTrue(istanbul.contains("İstanbul Modern"))
        assertFalse(london.contains("emniyetli"))
    }
}
