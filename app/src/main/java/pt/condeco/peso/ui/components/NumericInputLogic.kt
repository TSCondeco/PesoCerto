package pt.condeco.peso.ui.components

import java.util.Locale

/** Applies one keypad press ("0"-"9", ".", or "del") to [current]. Pure, so it is unit-tested directly. */
fun handleInput(current: String, input: String): String {
    if (input == "del") {
        if (current.length <= 1) return "0"
        return current.dropLast(1)
    }
    if (input == ".") {
        if (current.contains(".")) return current
        return current + "."
    }
    if (current == "0") return input
    return current + input
}

/** Adds [delta] to the numeric text [current], clamped at zero and rounded to [decimals] places. */
fun step(current: String, delta: Float, decimals: Int = 1): String {
    val value = (current.toFloatOrNull() ?: 0f) + delta
    return String.format(Locale.ROOT, "%.${decimals}f", value.coerceAtLeast(0f))
}
