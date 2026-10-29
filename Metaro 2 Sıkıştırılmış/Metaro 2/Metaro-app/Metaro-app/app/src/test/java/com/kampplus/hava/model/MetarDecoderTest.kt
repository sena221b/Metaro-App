package com.kampplus.hava.model

import org.junit.Assert.*
import org.junit.Test

class MetarDecoderTest {
    @Test fun `international report preserves precision and negative temperature`() {
        val decoded = MetarDecoderEngine.decode("METAR LTFM 281820Z 21012G22KT 4500 BKN012 M02/M05 Q1013")
        assertEquals("LTFM", decoded.stationIcao)
        assertEquals("4.5 km", decoded.visibilityKm)
        assertEquals(-2, decoded.temperatureC)
        assertEquals(FlightCategory.IFR, decoded.flightCategory)
        assertEquals(1200, decoded.ceilingFeet)
        assertEquals(22, decoded.wind.gustKnots)
    }
    @Test fun `US mixed fractions and altimeter are supported`() {
        val decoded = MetarDecoderEngine.decode("KJFK 281851Z 18005KT 1 1/2SM BR OVC008 12/10 A2992")
        assertEquals(FlightCategory.IFR, decoded.flightCategory)
        assertEquals("2.4 km", decoded.visibilityKm)
        assertTrue(decoded.altimeterQnh.startsWith("1013 hPa"))
    }
    @Test fun `visibility can make clear sky lifr`() {
        assertEquals(FlightCategory.LIFR, MetarDecoderEngine.decode("KJFK 281851Z 00000KT M1/4SM FG CLR 10/10 A2992").flightCategory)
    }
    @Test fun `boundary category is inclusive and trends are not current conditions`() {
        assertEquals(FlightCategory.MVFR, MetarDecoderEngine.decode("KJFK 281851Z 00000KT 5SM BKN030 10/10 A2992 TEMPO 1/4SM OVC001").flightCategory)
        assertEquals(FlightCategory.VFR, MetarDecoderEngine.decode("LTFM 281820Z 21012KT CAVOK 20/10 Q1013").flightCategory)
    }
    @Test fun `missing fields remain unknown rather than zero or vfr`() {
        val decoded = MetarDecoderEngine.decode("LTFM 281820Z NIL")
        assertNull(decoded.temperatureC)
        assertNull(decoded.wind.speedKnots)
        assertEquals(FlightCategory.UNKNOWN, decoded.flightCategory)
    }
    @Test fun `unknown vertical visibility does not invent vfr`() {
        assertEquals(FlightCategory.UNKNOWN, MetarDecoderEngine.decode("KJFK 281851Z 18005KT 6SM VV/// 12/10 A2992").flightCategory)
    }
    @Test fun `taf groups retain independent conditions and probabilities`() {
        val periods = TafDecoderEngine.decode("TAF LTFM 281700Z 2818/2924 21012KT 9999 SCT030 TEMPO 2820/2823 3000 RA BKN008 PROB30 TEMPO 2901/2904 TSRA BKN010CB FM290600 18005KT CAVOK")
        assertEquals(4, periods.size)
        assertTrue(periods[2].title.contains("30%"))
        assertNull(periods[1].conditions.wind.speedKnots)
        assertEquals("180", periods[3].conditions.wind.directionDegrees)
    }
}
