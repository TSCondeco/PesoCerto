package pt.condeco.peso.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.condeco.peso.MainActivity
import pt.condeco.peso.R
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDatabase

class WeightReminderReceiver : BroadcastReceiver() {

    private companion object {
        val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        // goAsync keeps the process alive while we read DataStore off the main thread
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = SettingsRepository(context)
                val settings = settingsRepository.settingsFlow.first()

                if (intent.action in RESCHEDULE_ACTIONS) {
                    // Alarms do not survive a reboot (and exact-alarm permission or clock changes
                    // invalidate them): reschedule if reminders are on
                    if (settings.remindersEnabled) {
                        ReminderManager(context).scheduleReminder(settings.reminderTime)
                    }
                    return@launch
                }

                if (!settings.remindersEnabled) return@launch

                // Always reschedule the next occurrence (exact alarms are one-shot)
                ReminderManager(context).scheduleReminder(settings.reminderTime)

                // Don't nag if today's weight is already logged
                val startOfToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val dao = WeightDatabase.getDatabase(context).weightDao()
                if (dao.countSince(startOfToday) > 0) return@launch

                val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()
                if (currentDay !in settings.reminderDays) return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }

                showNotification(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "weight_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ADD, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(0, context.getString(R.string.log_now), pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
