package com.huaman.bible_hymns_choruses.core.util

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import java.util.Calendar
import java.util.TimeZone

@Immutable
enum class DateSortOrder(val label: String) {
    NEWEST_FIRST("Más nuevos"),
    OLDEST_FIRST("Más antiguos")
}

private val SPANISH_MONTHS_SHORT = arrayOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

/**
 * Formats an epoch timestamp (in milliseconds) to the compact Spanish capsule format `"25dic 2025"`.
 */
fun formatShortSpanishDate(
    timestampMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = timestampMillis
    }
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val monthIndex = calendar.get(Calendar.MONTH).coerceIn(0, 11)
    val year = calendar.get(Calendar.YEAR)
    val monthAbbrev = SPANISH_MONTHS_SHORT[monthIndex]
    return "${day}${monthAbbrev} $year"
}

/**
 * Extracts the 4-digit year from an epoch timestamp.
 */
fun extractYear(
    timestampMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Int {
    val calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = timestampMillis
    }
    return calendar.get(Calendar.YEAR)
}

/**
 * Extracts the 1-based month number (1 = ene .. 12 = dic) from an epoch timestamp.
 */
fun extractMonth(
    timestampMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Int {
    val calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = timestampMillis
    }
    return calendar.get(Calendar.MONTH) + 1
}

fun spanishMonthAbbrev(month1Based: Int): String {
    return SPANISH_MONTHS_SHORT[(month1Based - 1).coerceIn(0, 11)]
}

/**
 * Shares plain text via Android's native system share sheet (`Intent.ACTION_SEND`).
 */
fun sharePlainText(
    context: Context,
    text: String,
    chooserTitle: String = "Compartir"
) {
    if (text.isBlank()) return
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(chooser)
    }
}
