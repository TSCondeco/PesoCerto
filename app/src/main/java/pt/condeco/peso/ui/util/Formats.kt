package pt.condeco.peso.ui.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Locale-aware date/time formatting built from skeletons, so each language gets its natural order. */
object Formats {
    private fun format(skeleton: String, millis: Long): String {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
        return SimpleDateFormat(pattern, locale).format(Date(millis))
    }

    fun dayMonth(millis: Long) = format("dMMM", millis)
    fun dayMonthYear(millis: Long) = format("dMMMy", millis)
    fun weekdayDayMonth(millis: Long) = format("EEEdMMM", millis)
    fun monthYear(millis: Long) = format("MMMMy", millis)
    fun day(millis: Long) = format("d", millis)
    fun monthShort(millis: Long) = format("MMM", millis)
    fun time(millis: Long) = format("jm", millis)

    /** One decimal place using the user's locale separator. */
    fun decimal(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)
}
