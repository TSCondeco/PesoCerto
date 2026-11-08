package pt.condeco.peso.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import pt.condeco.peso.data.WeightEntry

/** The granularity a streak is counted in, inferred from how often the user weighs in. */
enum class StreakPeriod { Day, Week, Month }

/** [current] and [longest] are counted in [period]s. */
data class Streaks(val current: Int, val longest: Int, val period: StreakPeriod = StreakPeriod.Day)

private const val CADENCE_SAMPLE_SIZE = 8
private const val MAX_DAILY_GAP_DAYS = 2
private const val MAX_WEEKLY_GAP_DAYS = 10

/**
 * Picks the streak period from the median gap between the user's most recent weigh-ins,
 * so someone weighing weekly is tracked in weeks rather than being permanently at zero days.
 */
fun detectStreakPeriod(days: List<LocalDate>): StreakPeriod {
    val recent = days.distinct().sorted().takeLast(CADENCE_SAMPLE_SIZE)
    if (recent.size < 2) return StreakPeriod.Day
    val gaps = recent.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }.sorted()
    val median = gaps[gaps.size / 2]
    return when {
        median <= MAX_DAILY_GAP_DAYS -> StreakPeriod.Day
        median <= MAX_WEEKLY_GAP_DAYS -> StreakPeriod.Week
        else -> StreakPeriod.Month
    }
}

/** Maps a date to a sequential index, so consecutive periods differ by exactly 1. */
private fun LocalDate.periodIndex(period: StreakPeriod, firstDayOfWeek: DayOfWeek): Long = when (period) {
    StreakPeriod.Day -> toEpochDay()
    StreakPeriod.Week -> Math.floorDiv(with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).toEpochDay(), 7L)
    StreakPeriod.Month -> year * 12L + (monthValue - 1)
}

/**
 * A streak is a run of consecutive periods (days, weeks or months, see [detectStreakPeriod])
 * with at least one entry. The current period not being logged yet doesn't break the streak.
 */
fun computeStreaks(
    sortedEntries: List<WeightEntry>,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
): Streaks {
    if (sortedEntries.isEmpty()) return Streaks(0, 0)
    val days = sortedEntries.map { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
    val period = detectStreakPeriod(days)
    val indices = days.map { it.periodIndex(period, firstDayOfWeek) }.distinct().sorted()

    var longest = 1
    var run = 1
    for (i in 1 until indices.size) {
        run = if (indices[i - 1] + 1 == indices[i]) run + 1 else 1
        if (run > longest) longest = run
    }

    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().periodIndex(period, firstDayOfWeek)
    val logged = indices.toSet()
    var cursor = if (today in logged) today else today - 1
    var current = 0
    while (cursor in logged) {
        current++
        cursor--
    }
    return Streaks(current, longest, period)
}
