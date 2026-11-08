package pt.condeco.peso

import java.time.DayOfWeek
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.StreakPeriod
import pt.condeco.peso.domain.computeStreaks

class StreaksTest {
    private val day = 24L * 60 * 60 * 1000

    // A Wednesday at noon UTC, so week boundaries are unambiguous.
    private val now = 1_800_000_000_000L - 1_800_000_000_000L % day + 12 * 3600_000L

    private fun entries(vararg daysAgo: Int) =
        daysAgo.sortedDescending().map { WeightEntry(weight = 80f, date = now - it * day) }

    private fun streaks(vararg daysAgo: Int) =
        computeStreaks(entries(*daysAgo), now, ZoneOffset.UTC, DayOfWeek.MONDAY)

    @Test
    fun weeklyWeigherGetsWeekStreak() {
        val result = streaks(28, 21, 14, 7, 0)
        assertEquals(StreakPeriod.Week, result.period)
        assertEquals(5, result.current)
        assertEquals(5, result.longest)
    }

    @Test
    fun weeklyStreakSurvivesThisWeekNotYetLogged() {
        val result = streaks(21, 14, 7)
        assertEquals(StreakPeriod.Week, result.period)
        assertEquals(3, result.current)
    }

    @Test
    fun weeklyStreakBreaksAfterSkippedWeek() {
        val result = streaks(35, 28, 14, 7)
        assertEquals(StreakPeriod.Week, result.period)
        assertEquals(2, result.current)
        assertEquals(2, result.longest)
    }

    @Test
    fun twoEntriesInSameWeekCountOnce() {
        val result = streaks(14, 8, 7, 0)
        assertEquals(StreakPeriod.Week, result.period)
        assertEquals(3, result.current)
    }

    @Test
    fun monthlyWeigherGetsMonthStreak() {
        val result = streaks(90, 60, 30, 0)
        assertEquals(StreakPeriod.Month, result.period)
        assertTrue(result.current >= 1)
    }

    @Test
    fun dailyWeigherStillGetsDayStreak() {
        val result = streaks(3, 2, 1, 0)
        assertEquals(StreakPeriod.Day, result.period)
        assertEquals(4, result.current)
    }

    @Test
    fun singleEntryDefaultsToDays() {
        assertEquals(StreakPeriod.Day, streaks(0).period)
        assertEquals(1, streaks(0).current)
    }
}
