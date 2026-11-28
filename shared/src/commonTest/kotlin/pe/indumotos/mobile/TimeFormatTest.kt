package pe.indumotos.mobile

import kotlin.test.*
import pe.indumotos.mobile.domain.limaTimestamp

class TimeFormatTest {
    @Test
    fun serverUtcTimeIsShownInLimaIncludingThePreviousDay() {
        assertEquals("23/11/2026 22:30", limaTimestamp("2026-11-24T03:30:29.491675Z"))
        assertEquals("19/11/2026 16:12", limaTimestamp("2026-11-19T21:12:33.43653Z"))
    }

    @Test
    fun explicitOffsetsAndYearBoundaryAreRespected() {
        assertEquals("31/12/2025 21:00", limaTimestamp("2026-01-01T02:00:00Z"))
        assertEquals("24/11/2026 08:00", limaTimestamp("2026-11-24T08:00:00-05:00"))
    }

    @Test
    fun valuesWithoutZoneAreOnlyReorderedAndUnknownTextIsKept() {
        assertEquals("24/11/2026 10:15", limaTimestamp("2026-11-24T10:15:00"))
        assertEquals("24/11/2026", limaTimestamp("2026-11-24"))
        assertEquals("sin fecha", limaTimestamp("sin fecha"))
    }
}
