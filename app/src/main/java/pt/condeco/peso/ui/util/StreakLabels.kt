package pt.condeco.peso.ui.util

import android.content.Context
import pt.condeco.peso.R
import pt.condeco.peso.domain.StreakPeriod
import pt.condeco.peso.domain.Streaks

fun Context.currentStreakText(streaks: Streaks): String {
    val plural = when (streaks.period) {
        StreakPeriod.Day -> R.plurals.streak_current_days
        StreakPeriod.Week -> R.plurals.streak_current_weeks
        StreakPeriod.Month -> R.plurals.streak_current_months
    }
    return resources.getQuantityString(plural, streaks.current, streaks.current)
}

fun Context.longestStreakText(streaks: Streaks): String {
    val plural = when (streaks.period) {
        StreakPeriod.Day -> R.plurals.streak_longest_days
        StreakPeriod.Week -> R.plurals.streak_longest_weeks
        StreakPeriod.Month -> R.plurals.streak_longest_months
    }
    return resources.getQuantityString(plural, streaks.longest, streaks.longest)
}
