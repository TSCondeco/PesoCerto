package pt.condeco.peso.domain

/** WHO adult BMI categories. */
enum class BmiCategory {
    Underweight, Normal, Overweight, Obese;

    companion object {
        const val UNDERWEIGHT_BELOW = 18.5f
        const val OVERWEIGHT_FROM = 25.0f
        const val OBESE_FROM = 30.0f

        fun of(bmi: Float): BmiCategory = when {
            bmi < UNDERWEIGHT_BELOW -> Underweight
            bmi < OVERWEIGHT_FROM -> Normal
            bmi < OBESE_FROM -> Overweight
            else -> Obese
        }
    }
}

fun computeBmi(weightKg: Float, heightCm: Int): Float =
    if (heightCm <= 0) 0f else weightKg / (heightCm / 100f).let { it * it }
