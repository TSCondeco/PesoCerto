package pt.condeco.peso.domain

import kotlin.math.roundToInt

/**
 * All data is stored in metric (kg, cm). Conversion to imperial happens only
 * at the UI boundary via these helpers.
 */
object Units {

    private const val KG_TO_LB = 2.20462262185f
    private const val CM_PER_INCH = 2.54f

    fun isMetric(unitSystem: UnitSystem): Boolean = unitSystem == UnitSystem.Metric

    fun weightLabel(unitSystem: UnitSystem): String = if (isMetric(unitSystem)) "kg" else "lb"

    fun toDisplayWeight(kg: Float, unitSystem: UnitSystem): Float =
        if (isMetric(unitSystem)) kg else kg * KG_TO_LB

    fun fromDisplayWeight(value: Float, unitSystem: UnitSystem): Float =
        if (isMetric(unitSystem)) value else value / KG_TO_LB

    fun formatWeight(kg: Float, unitSystem: UnitSystem): String =
        "%.1f".format(toDisplayWeight(kg, unitSystem))

    /** Like [formatWeight] for a difference: positive values get an explicit "+". */
    fun formatWeightDelta(kgDelta: Float, unitSystem: UnitSystem): String =
        (if (kgDelta > 0f) "+" else "") + formatWeight(kgDelta, unitSystem)

    fun formatHeight(cm: Int, unitSystem: UnitSystem): String =
        if (isMetric(unitSystem)) {
            "$cm cm"
        } else {
            val totalInches = cm / CM_PER_INCH
            val feet = (totalInches / 12).toInt()
            val inches = (totalInches - feet * 12).roundToInt()
            "$feet'$inches\""
        }

    /** Parses a height dialog value (cm or inches, depending on [unitSystem]) into cm. */
    fun heightInputToCm(value: Float, unitSystem: UnitSystem): Int =
        if (isMetric(unitSystem)) value.roundToInt()
        else (value * CM_PER_INCH).roundToInt()

    /** Stored cm as the height input value (cm or inches, depending on [unitSystem]). */
    fun cmToDisplayHeight(cm: Int, unitSystem: UnitSystem): Float =
        if (isMetric(unitSystem)) cm.toFloat() else cm / CM_PER_INCH

    /** Plausible height bounds, expressed in the height dialog's unit (cm or inches). */
    fun heightInputRange(unitSystem: UnitSystem): ClosedFloatingPointRange<Float> =
        if (isMetric(unitSystem)) 50f..250f else (50f / CM_PER_INCH)..(250f / CM_PER_INCH)

    /** Plausible goal-weight bounds, expressed in the display unit. */
    fun goalWeightRange(unitSystem: UnitSystem): ClosedFloatingPointRange<Float> =
        toDisplayWeight(20f, unitSystem)..toDisplayWeight(500f, unitSystem)
}
