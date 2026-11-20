package com.example.notifications

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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.MainActivity
import com.example.R
import com.example.domain.RpgNotificationEngine
import com.example.domain.RpgNotificationPayload
import com.example.domain.RpgNotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "solstice_notification_prefs"
)

data class NotificationSettings(
    val remindersEnabled: Boolean = true,
    val goalAlertsEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val lastHeroName: String = "Viajante",
    val lastSafeToSpendCents: Long = 0L,
    val lastLanguageLevel: Int = 2,
    val seenMilestoneIds: Set<String> = emptySet(),
    val seededUids: Set<String> = emptySet()
)

class NotificationPreferencesStore(private val context: Context) {

    private object Keys {
        val REMINDERS_ENABLED = booleanPreferencesKey("notif_reminders_enabled")
        val GOAL_ALERTS_ENABLED = booleanPreferencesKey("notif_goal_alerts_enabled")
        val REMINDER_HOUR = intPreferencesKey("notif_reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("notif_reminder_minute")
        val LAST_HERO_NAME = stringPreferencesKey("notif_last_hero_name")
        val LAST_SAFE_TO_SPEND_CENTS = longPreferencesKey("notif_last_safe_to_spend_cents")
        val LAST_LANGUAGE_LEVEL = intPreferencesKey("notif_last_language_level")
        val SEEN_MILESTONE_IDS = stringSetPreferencesKey("notif_seen_milestone_ids")
        val SEEDED_UIDS = stringSetPreferencesKey("notif_seeded_uids")
    }

    val settingsFlow: Flow<NotificationSettings> = context.notificationDataStore.data.map { prefs ->
        NotificationSettings(
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
            goalAlertsEnabled = prefs[Keys.GOAL_ALERTS_ENABLED] ?: true,
            reminderHour = (prefs[Keys.REMINDER_HOUR] ?: 20).coerceIn(0, 23),
            reminderMinute = (prefs[Keys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59),
            lastHeroName = prefs[Keys.LAST_HERO_NAME] ?: "Viajante",
            lastSafeToSpendCents = prefs[Keys.LAST_SAFE_TO_SPEND_CENTS] ?: 0L,
            lastLanguageLevel = (prefs[Keys.LAST_LANGUAGE_LEVEL] ?: 2).coerceIn(1, 3),
            seenMilestoneIds = prefs[Keys.SEEN_MILESTONE_IDS] ?: emptySet(),
            seededUids = prefs[Keys.SEEDED_UIDS] ?: emptySet()
        )
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setGoalAlertsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[Keys.GOAL_ALERTS_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.notificationDataStore.edit {
            it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun syncHeroContext(heroName: String, safeToSpendCents: Long, languageLevel: Int) {
        context.notificationDataStore.edit {
            it[Keys.LAST_HERO_NAME] = heroName
            it[Keys.LAST_SAFE_TO_SPEND_CENTS] = safeToSpendCents
            it[Keys.LAST_LANGUAGE_LEVEL] = languageLevel.coerceIn(1, 3)
        }
    }

    suspend fun markUidSeeded(uid: String, initialMilestoneIds: Set<String>) {
        context.notificationDataStore.edit { prefs ->
            val currentSeeded = prefs[Keys.SEEDED_UIDS] ?: emptySet()
            if (uid !in currentSeeded) {
                prefs[Keys.SEEDED_UIDS] = currentSeeded + uid
                val existingIds = prefs[Keys.SEEN_MILESTONE_IDS] ?: emptySet()
                prefs[Keys.SEEN_MILESTONE_IDS] = existingIds + initialMilestoneIds.map { "$uid:$it" }
            }
        }
    }

    suspend fun markMilestonesSeen(uid: String, milestoneIds: List<String>) {
        if (milestoneIds.isEmpty()) return
        context.notificationDataStore.edit { prefs ->
            val existing = prefs[Keys.SEEN_MILESTONE_IDS] ?: emptySet()
            prefs[Keys.SEEN_MILESTONE_IDS] = existing + milestoneIds.map { "$uid:$it" }
        }
    }
}

object RpgNotificationDispatcher {

    const val CHANNEL_DAILY_QUEST = "solstice_daily_quest"
    const val CHANNEL_GOAL_VICTORY = "solstice_goal_victory"
    const val EXTRA_OPEN_TARGET = "solstice_open_target"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_QUEST,
                "Missões Diárias da Taverna",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes lúdicos em clima RPG para registrar suas despesas do dia na Forja."
            }

            val victoryChannel = NotificationChannel(
                CHANNEL_GOAL_VICTORY,
                "Conquistas & Metas Financeiras",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas comemorativos quando metas, reservas, níveis de Sabedoria ou trajes são atingidos."
            }

            manager.createNotificationChannel(dailyChannel)
            manager.createNotificationChannel(victoryChannel)
        }
    }

    fun dispatch(context: Context, payload: RpgNotificationPayload): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }
        ensureChannels(context)

        val channelId = if (payload.type == RpgNotificationType.DAILY_EXPENSE_REMINDER) {
            CHANNEL_DAILY_QUEST
        } else {
            CHANNEL_GOAL_VICTORY
        }
        val priority = if (payload.type == RpgNotificationType.DAILY_EXPENSE_REMINDER) {
            NotificationCompat.PRIORITY_DEFAULT
        } else {
            NotificationCompat.PRIORITY_HIGH
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_TARGET, payload.openTarget)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_solstice_notify)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setSubText(payload.badgeLabel)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(payload.notificationId, notification)
            true
        } catch (se: SecurityException) {
            Log.w("RpgNotification", "POST_NOTIFICATIONS denied during notify", se)
            false
        }
    }
}

object RpgReminderScheduler {

    private const val DAILY_ALARM_REQUEST_CODE = 4201

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildReminderPendingIntent(context)

        val triggerAt = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(buildReminderPendingIntent(context))
    }

    private fun buildReminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, RpgReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class RpgReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = NotificationPreferencesStore(appContext).settingsFlow.first()
                if (settings.remindersEnabled) {
                    val payload = RpgNotificationEngine.buildDailyReminderCopy(
                        heroName = settings.lastHeroName,
                        safeToSpendDailyCents = settings.lastSafeToSpendCents,
                        languageLevel = settings.lastLanguageLevel
                    )
                    RpgNotificationDispatcher.dispatch(appContext, payload)
                }
            } catch (e: Exception) {
                Log.e("RpgReminderReceiver", "Failed to dispatch daily RPG reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
