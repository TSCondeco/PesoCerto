package pt.condeco.peso.ui.analytics

import kotlin.math.roundToInt

/** X position of point [index] among [count] evenly spaced points across [width]. */
internal fun pointX(index: Int, count: Int, width: Float): Float =
    if (count > 1) index.toFloat() / (count - 1) * width else width / 2

/** Maps weights and entry indices onto a canvas of the given pixel size. */
internal class ChartScale(weights: List<Float>, private val width: Float, private val height: Float) {
    private val minWeight = weights.min() - PADDING
    private val range = weights.max() + PADDING - minWeight

    fun x(index: Int, count: Int): Float = pointX(index, count, width)

    fun y(weight: Float): Float = height - (weight - minWeight) / range * height

    private companion object {
        const val PADDING = 1f
    }
}

/** Index of the entry whose x position is closest to [x], for [count] evenly spaced points. */
internal fun nearestIndex(x: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    return (x / width * (count - 1)).roundToInt().coerceIn(0, count - 1)
}
