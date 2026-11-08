package pt.condeco.peso.domain

/** Measurement system used to display weights and heights. Stored values are always metric. */
enum class UnitSystem {
    Metric, Imperial;

    companion object {
        fun fromName(name: String?): UnitSystem = entries.firstOrNull { it.name == name } ?: Metric
    }
}

enum class BiologicalSex {
    Male, Female, Other;

    companion object {
        fun fromName(name: String?): BiologicalSex = entries.firstOrNull { it.name == name } ?: Male
    }
}

enum class ThemeMode {
    System, Light, Dark;

    companion object {
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: System
    }
}

/**
 * Whether a [delta] (in kg) moves the weight toward [goalKg] from [currentKg].
 * At the goal itself, any change is treated as neutral/good.
 */
fun isTowardGoal(currentKg: Float, goalKg: Float, delta: Float): Boolean = when {
    currentKg > goalKg -> delta <= 0f
    currentKg < goalKg -> delta >= 0f
    else -> true
}
