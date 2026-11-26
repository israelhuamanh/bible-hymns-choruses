package com.huaman.bible_hymns_choruses

import com.huaman.bible_hymns_choruses.core.util.DateSortOrder
import com.huaman.bible_hymns_choruses.core.util.extractMonth
import com.huaman.bible_hymns_choruses.core.util.extractYear
import com.huaman.bible_hymns_choruses.core.util.formatShortSpanishDate
import com.huaman.bible_hymns_choruses.core.util.spanishMonthAbbrev
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatterAndSortingTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun epochFor(year: Int, month1Based: Int, day: Int): Long {
        return Calendar.getInstance(utc).apply {
            clear()
            set(year, month1Based - 1, day, 12, 0, 0)
        }.timeInMillis
    }

    @Test
    fun `formatShortSpanishDate formats date in 25dic 2025 style`() {
        val dec25 = epochFor(2025, 12, 25)
        assertEquals("25dic 2025", formatShortSpanishDate(dec25, utc))

        val mar14 = epochFor(2026, 3, 14)
        assertEquals("14mar 2026", formatShortSpanishDate(mar14, utc))

        val jan5 = epochFor(2024, 1, 5)
        assertEquals("5ene 2024", formatShortSpanishDate(jan5, utc))
    }

    @Test
    fun `extractYear and extractMonth support accurate year and month filtering`() {
        val ts = epochFor(2025, 12, 25)
        assertEquals(2025, extractYear(ts, utc))
        assertEquals(12, extractMonth(ts, utc))
        assertEquals("dic", spanishMonthAbbrev(12))
    }

    @Test
    fun `DateSortOrder sorts timestamps newest first and oldest first`() {
        val t1 = epochFor(2024, 6, 10)
        val t2 = epochFor(2025, 12, 25)
        val t3 = epochFor(2026, 1, 15)
        val list = listOf(t2, t1, t3)

        val newestFirst = when (DateSortOrder.NEWEST_FIRST) {
            DateSortOrder.NEWEST_FIRST -> list.sortedDescending()
            DateSortOrder.OLDEST_FIRST -> list.sorted()
        }
        assertEquals(listOf(t3, t2, t1), newestFirst)

        val oldestFirst = when (DateSortOrder.OLDEST_FIRST) {
            DateSortOrder.NEWEST_FIRST -> list.sortedDescending()
            DateSortOrder.OLDEST_FIRST -> list.sorted()
        }
        assertEquals(listOf(t1, t2, t3), oldestFirst)
    }
}
