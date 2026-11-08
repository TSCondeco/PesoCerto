package pt.condeco.peso.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.condeco.peso.MainActivity
import pt.condeco.peso.R
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDatabase
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.computeGoalInfo
import pt.condeco.peso.domain.computeStreaks
import pt.condeco.peso.ui.util.currentStreakText

/** 4x2 widget: latest weight, goal progress, streak and a 30-day trend chart. */
class WeightChartWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                update(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val CHART_DAYS = 30L

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WeightChartWidgetProvider::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch { update(context, manager, ids) }
        }

        private suspend fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val entries = WeightDatabase.getDatabase(context).weightDao().getAllOnce()
            val settings = SettingsRepository(context).settingsFlow.first()
            val unit = settings.unitSystem
            val now = System.currentTimeMillis()

            val views = RemoteViews(context.packageName, R.layout.widget_weight_chart)
            val latest = entries.lastOrNull()
            views.setTextViewText(
                R.id.widget_weight,
                if (latest != null) "${Units.formatWeight(latest.weight, unit)} ${Units.weightLabel(unit)}" else "--"
            )

            val info = computeGoalInfo(entries, settings.goalWeight, now)
            views.setTextViewText(
                R.id.widget_subtitle,
                info?.let { context.getString(R.string.widget_goal_percent, (it.progress * 100).toInt()) } ?: ""
            )
            val streaks = computeStreaks(entries, now)
            views.setTextViewText(
                R.id.widget_streak,
                if (streaks.current > 0) context.currentStreakText(streaks) else ""
            )

            val recent = entries.filter { it.date > now - CHART_DAYS * 24 * 60 * 60 * 1000 }
            views.setImageViewBitmap(
                R.id.widget_chart,
                ChartBitmap.render(
                    recent,
                    if (recent.isNotEmpty()) settings.goalWeight else null,
                    context.getColor(R.color.widget_accent),
                    context.getColor(R.color.widget_text_secondary)
                )
            )

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_ADD, true)
            }
            val addIntent = PendingIntent.getActivity(
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add, addIntent)

            val openIntent = PendingIntent.getActivity(
                context, 3, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openIntent)

            manager.updateAppWidget(ids, views)
        }
    }
}
