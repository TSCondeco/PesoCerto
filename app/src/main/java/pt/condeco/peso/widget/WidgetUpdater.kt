package pt.condeco.peso.widget

import android.content.Context

/** Triggers a redraw of every placed home-screen widget. */
class WidgetUpdater(private val context: Context) {
    fun refreshAll() {
        WeightWidgetProvider.refresh(context)
        WeightChartWidgetProvider.refresh(context)
    }
}
