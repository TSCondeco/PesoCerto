package pt.condeco.peso

import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.WeightCsv
import pt.condeco.peso.domain.computeGoalInfo
import pt.condeco.peso.domain.computeStats
import pt.condeco.peso.domain.computeStreaks
import pt.condeco.peso.domain.rangeStart
import pt.condeco.peso.domain.trendSlopePerDay
import pt.condeco.peso.ui.TimeRange

class WeightStatsTest {
    private val day = 24L * 60 * 60 * 1000
    private val now = 1_800_000_000_000L

    private fun entry(daysAgo: Int, weight: Float) =
        WeightEntry(weight = weight, date = now - daysAgo * day)

    @Test
    fun averagesUseDaysNotEntryCount() {
        val entries = listOf(
            entry(20, 90f), entry(10, 80f), entry(6, 70f), entry(2, 72f), entry(1, 74f)
        ).sortedBy { it.date }
        val stats = computeStats(entries, now)
        assertEquals(72f, stats.avg7d, 0.001f)
        assertEquals(80f, stats.prevAvg7d, 0.001f)
        assertEquals(77.2f, stats.avg30d, 0.001f)
    }

    @Test
    fun netChangeIsLastMinusFirst() {
        val stats = computeStats(listOf(entry(5, 80f), entry(0, 78f)), now)
        assertEquals(-2f, stats.netChange, 0.001f)
    }

    @Test
    fun emptyEntriesGiveZeros() {
        val stats = computeStats(emptyList(), now)
        assertEquals(0f, stats.avg7d, 0f)
        assertEquals(0f, stats.netChange, 0f)
    }

    @Test
    fun ytdStartsOnJanuaryFirst() {
        val cal = Calendar.getInstance().apply { timeInMillis = rangeStart(TimeRange.YTD, now) }
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun slopeIsNegativeWhenLosing() {
        val entries = listOf(entry(10, 80f), entry(5, 79f), entry(0, 78f))
        assertEquals(-0.2f, trendSlopePerDay(entries, now)!!, 0.001f)
    }

    @Test
    fun slopeNeedsTwoEntries() {
        assertNull(trendSlopePerDay(listOf(entry(1, 80f)), now))
    }

    @Test
    fun goalProgressAndProjection() {
        val entries = listOf(entry(10, 80f), entry(5, 79f), entry(0, 78f))
        val info = computeGoalInfo(entries, 70f, now)!!
        assertEquals(0.2f, info.progress, 0.001f)
        assertFalse(info.reached)
        assertEquals(-1.4f, info.weeklyRate!!, 0.001f)
        // 8 kg left at 0.2 kg/day = 40 days
        assertEquals((now + 40 * day).toDouble(), info.projectedDate!!.toDouble(), day / 100.0)
    }

    @Test
    fun noProjectionWhenMovingAwayFromGoal() {
        val entries = listOf(entry(10, 78f), entry(0, 80f))
        assertNull(computeGoalInfo(entries, 70f, now)!!.projectedDate)
    }

    @Test
    fun goalReachedGivesFullProgress() {
        val entries = listOf(entry(10, 80f), entry(0, 69f))
        val info = computeGoalInfo(entries, 70f, now)!!
        assertTrue(info.reached)
        assertEquals(1f, info.progress, 0f)
        assertNull(info.projectedDate)
        assertEquals(10, info.milestoneKg)
    }

    @Test
    fun noGoalInfoWithoutEntries() {
        assertNotNull(computeGoalInfo(listOf(entry(0, 80f)), 70f, now))
        assertNull(computeGoalInfo(emptyList(), 70f, now))
    }

    @Test
    fun streakCountsConsecutiveDaysAndSurvivesUnloggedToday() {
        val noon = 1_800_000_000_000L - 1_800_000_000_000L % day + 12 * 3600_000L
        fun e(daysAgo: Int) = WeightEntry(weight = 80f, date = noon - daysAgo * day)
        val entries = listOf(e(10), e(9), e(8), e(7), e(3), e(2), e(1))
        val streaks = computeStreaks(entries, noon, ZoneOffset.UTC)
        assertEquals(3, streaks.current)
        assertEquals(4, streaks.longest)
    }

    @Test
    fun streakBreaksAfterMissedDay() {
        val noon = 1_800_000_000_000L - 1_800_000_000_000L % day + 12 * 3600_000L
        val entries = listOf(WeightEntry(weight = 80f, date = noon - 2 * day))
        assertEquals(0, computeStreaks(entries, noon, ZoneOffset.UTC).current)
    }

    @Test
    fun csvRoundTrips() {
        val entries = listOf(entry(3, 80.5f), entry(1, 79.8f))
        val csv = WeightCsv.export(entries, Locale.UK)
        val parsed = WeightCsv.parse(csv, Locale.UK)
        assertEquals(2, parsed.size)
        assertEquals(80.5f, parsed[0].weight, 0.001f)
        assertEquals(79.8f, parsed[1].weight, 0.001f)
    }

    @Test
    fun csvSkipsBadLines() {
        val parsed = WeightCsv.parse("Data,Peso\nbad,line\n1/2/2024,\"80,5\"\n\n", Locale.UK)
        assertEquals(1, parsed.size)
        assertEquals(80.5f, parsed[0].weight, 0.001f)
    }
}
