package pt.condeco.peso.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pt.condeco.peso.R
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.components.NumericEntrySheet

private const val HEIGHT_STEP_CM = 1f
private const val HEIGHT_STEP_INCHES = 0.5f
private const val WEIGHT_STEP = 0.1f

@Composable
fun HeightSheet(heightCm: Int, unit: UnitSystem, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val metric = Units.isMetric(unit)
    NumericEntrySheet(
        title = stringResource(R.string.update_height),
        initialValue = Units.cmToDisplayHeight(heightCm, unit),
        unitLabel = stringResource(if (metric) R.string.unit_cm else R.string.unit_in),
        stepSize = if (metric) HEIGHT_STEP_CM else HEIGHT_STEP_INCHES,
        decimals = if (metric) 0 else 1,
        range = Units.heightInputRange(unit),
        onConfirm = { onConfirm(Units.heightInputToCm(it, unit)) },
        onDismiss = onDismiss
    )
}

@Composable
fun GoalWeightSheet(goalKg: Float, unit: UnitSystem, onConfirm: (Float) -> Unit, onDismiss: () -> Unit) {
    NumericEntrySheet(
        title = stringResource(R.string.update_goal),
        initialValue = Units.toDisplayWeight(goalKg, unit),
        unitLabel = Units.weightLabel(unit),
        stepSize = WEIGHT_STEP,
        decimals = 1,
        range = Units.goalWeightRange(unit),
        onConfirm = { onConfirm(Units.fromDisplayWeight(it, unit)) },
        onDismiss = onDismiss
    )
}
