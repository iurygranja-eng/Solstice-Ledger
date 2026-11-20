package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSettings
import com.example.audio.RpgAudioEngine
import com.example.domain.AvatarEvolutionEngine
import com.example.domain.RpgNotificationEngine
import com.example.domain.RpgNotificationPayload
import com.example.notifications.NotificationSettings
import com.example.notifications.RpgNotificationDispatcher
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.viewmodel.LedgerUiState

private data class ReminderSlot(val hour: Int, val minute: Int, val label: String)

private val reminderTimeSlots = listOf(
    ReminderSlot(12, 0, "12:00"),
    ReminderSlot(19, 0, "19:00"),
    ReminderSlot(20, 0, "20:00"),
    ReminderSlot(21, 30, "21:30")
)

@Composable
fun NotificationSettingsCard(
    state: LedgerUiState.Ready,
    audioSettings: AudioSettings,
    notificationSettings: NotificationSettings,
    onToggleReminders: (Boolean) -> Unit,
    onToggleGoalAlerts: (Boolean) -> Unit,
    onSelectReminderTime: (Int, Int) -> Unit,
    onFeedbackMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(RpgNotificationDispatcher.hasNotificationPermission(context))
    }
    var pendingPayloadAfterGrant by remember { mutableStateOf<RpgNotificationPayload?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            RpgAudioEngine.playSuccessChime(audioSettings)
            val pending = pendingPayloadAfterGrant
            pendingPayloadAfterGrant = null
            if (pending != null) {
                RpgNotificationDispatcher.dispatch(context, pending)
                onFeedbackMessage("Notificação RPG enviada para a barra do sistema!")
            } else {
                onFeedbackMessage("Permissão concedida! Missões e conquistas RPG ativadas.")
            }
        } else {
            pendingPayloadAfterGrant = null
            onFeedbackMessage("Permissão negada. Você pode ativar as notificações nas configurações do Android.")
        }
    }

    val evolution = remember(state.profile) {
        AvatarEvolutionEngine.compute(state.profile)
    }
    val previewReminder = remember(
        state.profile.heroName,
        state.stats.safeToSpendDailyCents,
        state.profile.languageLevel
    ) {
        RpgNotificationEngine.buildDailyReminderCopy(
            heroName = state.profile.heroName,
            safeToSpendDailyCents = state.stats.safeToSpendDailyCents,
            languageLevel = state.profile.languageLevel
        )
    }

    fun dispatchOrRequestPermission(payload: RpgNotificationPayload) {
        if (RpgNotificationDispatcher.hasNotificationPermission(context)) {
            hasPermission = true
            val sent = RpgNotificationDispatcher.dispatch(context, payload)
            if (sent) {
                RpgAudioEngine.playSuccessChime(audioSettings)
                onFeedbackMessage("Push enviado: ${payload.title}")
            } else {
                onFeedbackMessage("Não foi possível exibir a notificação agora.")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingPayloadAfterGrant = payload
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    RpgPsxWindow(
        modifier = modifier,
        borderColor = GoldBadge.copy(alpha = 0.6f),
        bracketColor = GoldBadge,
        contentPadding = PaddingValues(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RpgIconMedallion(
                    icon = if (hasPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    tint = if (hasPermission) GoldBadge else AmberWarn,
                    size = 32.dp,
                    iconSize = 18.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Arauto & Push Gamificado",
                        style = CinzelTitleStyle.copy(fontSize = 15.sp),
                        color = LunarPrimary
                    )
                    Text(
                        text = "Lembretes de gastos e vitórias em tom de RPG",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(
                text = if (hasPermission) "ATIVO" else "SEM PERMISSÃO",
                color = if (hasPermission) EmeraldSafe else AmberWarn
            )
        }

        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(Modifier.height(12.dp))
            RpgInsetWell(
                borderColor = AmberWarn.copy(alpha = 0.6f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Permita notificações para receber lembretes da Forja e alertas quando conquistar metas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceText
                    )
                    LunarCoreButton(
                        text = "Ativar Permissão de Notificações",
                        onClick = {
                            RpgAudioEngine.playUiTap(audioSettings)
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "notif_request_permission_button"
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Daily Expense Reminder Toggle + Time Picker
        RpgInsetWell(
            borderColor = if (notificationSettings.remindersEnabled) LunarCyan.copy(alpha = 0.55f) else BevelBorderIndigo,
            contentPadding = PaddingValues(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (notificationSettings.remindersEnabled) LunarCyan else OnSurfaceMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lembrete Diário de Despesas",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceText
                            )
                            Text(
                                text = "Chama você para registrar gastos do dia na Forja",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                    Switch(
                        checked = notificationSettings.remindersEnabled,
                        onCheckedChange = { enabled ->
                            RpgAudioEngine.playUiTap(audioSettings)
                            onToggleReminders(enabled)
                            if (enabled && !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MidnightBase,
                            checkedTrackColor = LunarCyan,
                            uncheckedThumbColor = OnSurfaceMuted,
                            uncheckedTrackColor = MidnightBase
                        ),
                        modifier = Modifier.testTag("notif_daily_reminder_switch")
                    )
                }

                if (notificationSettings.remindersEnabled) {
                    Text(
                        text = "HORÁRIO DA MISSÃO DIÁRIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = LunarCyan
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        reminderTimeSlots.forEach { slot ->
                            val selected = notificationSettings.reminderHour == slot.hour &&
                                notificationSettings.reminderMinute == slot.minute
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(7.dp))
                                    .clickable {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onSelectReminderTime(slot.hour, slot.minute)
                                        onFeedbackMessage("Lembrete diário agendado para as ${slot.label}.")
                                    }
                                    .testTag("notif_time_${slot.label.replace(":", "")}"),
                                shape = RoundedCornerShape(7.dp),
                                color = if (selected) CosmicIndigo else MidnightVoid,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) LunarCyan else BevelBorderIndigo
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = slot.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) LunarPrimary else OnSurfaceMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Goal Achievements Toggle
        RpgInsetWell(
            borderColor = if (notificationSettings.goalAlertsEnabled) GoldBadge.copy(alpha = 0.55f) else BevelBorderIndigo,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (notificationSettings.goalAlertsEnabled) GoldBadge else OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Alertas de Metas & Evolução RPG",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceText
                        )
                        Text(
                            text = "Notifica ao bater reservas, vencer o equilíbrio ou evoluir traje",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
                Switch(
                    checked = notificationSettings.goalAlertsEnabled,
                    onCheckedChange = { enabled ->
                        RpgAudioEngine.playUiTap(audioSettings)
                        onToggleGoalAlerts(enabled)
                        if (enabled && !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MidnightBase,
                        checkedTrackColor = GoldBadge,
                        uncheckedThumbColor = OnSurfaceMuted,
                        uncheckedTrackColor = MidnightBase
                    ),
                    modifier = Modifier.testTag("notif_goal_alerts_switch")
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Live RPG Copy Preview
        RpgInsetWell(
            borderColor = SolarMagenta.copy(alpha = 0.4f),
            contentPadding = PaddingValues(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PRÉVIA DO ARAUTO (NÍVEL ${state.profile.languageLevel})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = SolarMagenta
                )
                Text(
                    text = previewReminder.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = LunarPrimary
                )
                Text(
                    text = previewReminder.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Instant Push Test Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(7.dp))
                    .clickable {
                        dispatchOrRequestPermission(previewReminder)
                    }
                    .testTag("notif_test_reminder_button"),
                shape = RoundedCornerShape(7.dp),
                color = LunarCyan.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, LunarCyan.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Testar Lembrete",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LunarCyan
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(7.dp))
                    .clickable {
                        val victoryPayload = RpgNotificationEngine.buildSampleGoalVictoryCopy(
                            heroName = state.profile.heroName,
                            evolution = evolution,
                            languageLevel = state.profile.languageLevel
                        )
                        dispatchOrRequestPermission(victoryPayload)
                    }
                    .testTag("notif_test_goal_button"),
                shape = RoundedCornerShape(7.dp),
                color = GoldBadge.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, GoldBadge.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simular Meta 100%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldBadge
                    )
                }
            }
        }
    }
}
