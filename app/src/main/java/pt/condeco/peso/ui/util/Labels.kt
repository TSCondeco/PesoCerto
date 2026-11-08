package pt.condeco.peso.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pt.condeco.peso.R
import pt.condeco.peso.domain.BiologicalSex
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem

/** Localized display names for domain enums. */
@Composable
fun BiologicalSex.label(): String = stringResource(
    when (this) {
        BiologicalSex.Male -> R.string.male
        BiologicalSex.Female -> R.string.female
        BiologicalSex.Other -> R.string.other
    }
)

@Composable
fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.System -> R.string.theme_system
        ThemeMode.Light -> R.string.theme_light
        ThemeMode.Dark -> R.string.theme_dark
    }
)

@Composable
fun UnitSystem.label(): String = stringResource(
    when (this) {
        UnitSystem.Metric -> R.string.metric
        UnitSystem.Imperial -> R.string.imperial
    }
)
