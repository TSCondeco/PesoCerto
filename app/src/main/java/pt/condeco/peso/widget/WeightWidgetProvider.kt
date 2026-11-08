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
import pt.condeco.peso.domain.computeStreaks
import pt.condeco.peso.ui.util.currentStreakText

class WeightWidgetProvider : AppWidgetProvider() {

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
        /** Asks every placed widget to redraw, e.g. after the entries changed. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WeightWidgetProvider::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch { update(context, manager, ids) }
        }

        private suspend fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val entries = WeightDatabase.getDatabase(context).weightDao().getAllOnce()
            val unit = SettingsRepository(context).settingsFlow.first().unitSystem
            val now = System.currentTimeMillis()

            val views = RemoteViews(context.packageName, R.layout.widget_weight)
            val latest = entries.lastOrNull()
            views.setTextViewText(
                R.id.widget_weight,
                if (latest != null) "${Units.formatWeight(latest.weight, unit)} ${Units.weightLabel(unit)}" else "--"
            )
            val streaks = computeStreaks(entries, now)
            views.setTextViewText(
                R.id.widget_streak,
                if (streaks.current > 0) context.currentStreakText(streaks) else ""
            )

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_ADD, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_add, pendingIntent)

            manager.updateAppWidget(ids, views)
        }
    }
}
