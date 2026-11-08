package pt.condeco.peso.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(context, WeightReminderReceiver::class.java).apply {
            action = ACTION_WEIGHT_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Schedules the next reminder at [timeStr] ("HH:mm").
     *
     * Prefers an exact alarm that fires even in Doze (rescheduled by the receiver
     * after every fire), and falls back to an inexact repeating alarm when exact
     * alarms are not allowed. Any previously scheduled alarm is cancelled first so
     * alarms never stack up.
     */
    fun scheduleReminder(timeStr: String) {
        val pendingIntent = reminderPendingIntent()
        alarmManager.cancel(pendingIntent)

        val parts = timeStr.split(":")
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time is in the past today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    fun cancelReminder() {
        alarmManager.cancel(reminderPendingIntent())
    }

    companion object {
        const val ACTION_WEIGHT_REMINDER = "pt.condeco.peso.ACTION_WEIGHT_REMINDER"
        const val REQUEST_CODE = 0
    }
}
