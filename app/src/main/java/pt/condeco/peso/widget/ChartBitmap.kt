package pt.condeco.peso.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import pt.condeco.peso.data.WeightEntry

/** Renders a simple trend line (and optional dashed goal line) for use in RemoteViews. */
object ChartBitmap {

    fun render(
        entries: List<WeightEntry>,
        goalKg: Float?,
        lineColor: Int,
        goalColor: Int,
        width: Int = 640,
        height: Int = 240
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (entries.isEmpty()) return bitmap
        val canvas = Canvas(bitmap)
        val pad = 12f

        val weights = entries.map { it.weight } + listOfNotNull(goalKg)
        val min = weights.min() - 0.5f
        val max = weights.max() + 0.5f
        val range = max - min

        fun y(w: Float) = pad + (1 - (w - min) / range) * (height - 2 * pad)
        fun x(i: Int) = if (entries.size == 1) width / 2f else pad + i.toFloat() / (entries.size - 1) * (width - 2 * pad)

        if (goalKg != null) {
            val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = goalColor
                strokeWidth = 3f
                style = Paint.Style.STROKE
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(14f, 10f), 0f)
            }
            canvas.drawLine(pad, y(goalKg), width - pad, y(goalKg), goalPaint)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        if (entries.size > 1) {
            val path = Path()
            entries.forEachIndexed { i, e -> if (i == 0) path.moveTo(x(i), y(e.weight)) else path.lineTo(x(i), y(e.weight)) }
            canvas.drawPath(path, linePaint)
        }
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lineColor; style = Paint.Style.FILL }
        canvas.drawCircle(x(entries.lastIndex), y(entries.last().weight), 9f, dot)
        return bitmap
    }
}
