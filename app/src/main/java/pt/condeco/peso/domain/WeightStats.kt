package pt.condeco.peso.domain

import java.util.Calendar
import kotlin.math.abs
import kotlin.math.floor
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.ui.TimeRange

private const val DAY_MS = 24L * 60 * 60 * 1000

data class WeightStats(
    val avg7d: Float,
    val prevAvg7d: Float,
    val avg30d: Float,
    val netChange: Float
)

private fun average(entries: List<WeightEntry>): Float =
    if (entries.isEmpty()) 0f else entries.map { it.weight }.average().toFloat()

/** Averages are time-based: last 7 days, the 7 days before that, and the last 30 days. */
fun computeStats(sortedEntries: List<WeightEntry>, now: Long): WeightStats {
    val last7 = sortedEntries.filter { it.date > now - 7 * DAY_MS }
    val prev7 = sortedEntries.filter { it.date > now - 14 * DAY_MS && it.date <= now - 7 * DAY_MS }
    val last30 = sortedEntries.filter { it.date > now - 30 * DAY_MS }
    val net = if (sortedEntries.isNotEmpty()) sortedEntries.last().weight - sortedEntries.first().weight else 0f
    return WeightStats(average(last7), average(prev7), average(last30), net)
}

fun rangeStart(range: TimeRange, now: Long): Long = when (range) {
    TimeRange.W1 -> now - 7 * DAY_MS
    TimeRange.M1 -> now - 30 * DAY_MS
    TimeRange.M3 -> now - 90 * DAY_MS
    TimeRange.YTD -> Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    TimeRange.ALL -> 0L
}

const val MILESTONE_STEP_KG = 5

data class GoalInfo(
    val progress: Float,
    val reached: Boolean,
    /** Average change in kg per week over the recent trend window, or null with too little data. */
    val weeklyRate: Float?,
    val projectedDate: Long?,
    /** Largest multiple of [MILESTONE_STEP_KG] moved toward the goal since the first entry. */
    val milestoneKg: Int
)

/** Least-squares slope in kg/day over the last [windowDays] days; null if the data can't define a trend. */
fun trendSlopePerDay(sortedEntries: List<WeightEntry>, now: Long, windowDays: Int = 30): Float? {
    val window = sortedEntries.filter { it.date > now - windowDays * DAY_MS && it.date <= now }
    if (window.size < 2) return null
    val xs = window.map { (it.date - window.first().date).toDouble() / DAY_MS }
    val ys = window.map { it.weight.toDouble() }
    val meanX = xs.average()
    val meanY = ys.average()
    val denom = xs.sumOf { (it - meanX) * (it - meanX) }
    if (denom < 1e-9) return null
    val num = xs.indices.sumOf { (xs[it] - meanX) * (ys[it] - meanY) }
    return (num / denom).toFloat()
}

fun computeGoalInfo(sortedEntries: List<WeightEntry>, goal: Float, now: Long): GoalInfo? {
    val first = sortedEntries.firstOrNull() ?: return null
    val current = sortedEntries.last().weight
    val start = first.weight
    val losing = goal <= start
    val moved = if (losing) start - current else current - start
    val reached = if (losing) current <= goal else current >= goal
    val total = abs(start - goal)
    val progress = when {
        reached -> 1f
        total < 0.001f -> 1f
        else -> (moved / total).coerceIn(0f, 1f)
    }
    val slope = trendSlopePerDay(sortedEntries, now)
    val towardGoal = slope != null && !reached && ((goal < current && slope < -0.001f) || (goal > current && slope > 0.001f))
    val projected = if (towardGoal) {
        val days = (goal - current) / slope!!
        if (days in 0f..(5 * 365f)) now + (days * DAY_MS).toLong() else null
    } else null
    val milestone = if (moved > 0) floor(moved / MILESTONE_STEP_KG).toInt() * MILESTONE_STEP_KG else 0
    return GoalInfo(progress, reached, slope?.times(7), projected, milestone)
}
