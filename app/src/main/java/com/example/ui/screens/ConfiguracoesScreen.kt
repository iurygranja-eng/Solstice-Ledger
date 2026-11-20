package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSettings
import com.example.audio.RpgAudioEngine
import com.example.audio.SoundProfile
import com.example.data.safeEnumOf
import com.example.data.FinancialClass
import com.example.notifications.NotificationSettings
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.NotificationSettingsCard
import com.example.ui.components.ReserveShieldBadge
import com.example.ui.components.RpgIconMedallion
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgProgressBar
import com.example.ui.components.RpgPsxWindow
import com.example.ui.components.SolarCompanionButton
import com.example.ui.components.StatusChip
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
import kotlin.math.roundToInt

private data class LevelOptionInfo(
    val level: Int,
    val title: String,
    val badge: String,
    val description: String,
    val previewTerms: String,
    val accent: Color
)

private val levelOptions = listOf(
    LevelOptionInfo(
        level = 1,
        title = "Nível 1 — Fácil (Cotidiano)",
        badge = "DIRETO",
        description = "Linguagem simples e imediata para decisões do dia a dia, sem termos técnicos ou RPG.",
        previewTerms = "Quanto posso gastar hoje • Dinheiro em Conta • Folga após Contas",
        accent = EmeraldSafe
    ),
    LevelOptionInfo(
        level = 2,
        title = "Nível 2 — Médio (Financeiro)",
        badge = "CLARO",
        description = "Terminologia objetiva de finanças pessoais, fluxo de caixa e planejamento mensal.",
        previewTerms = "Orçamento Livre Diário • Liquidez Imediata • Ponto de Equilíbrio",
        accent = LunarCyan
    ),
    LevelOptionInfo(
        level = 3,
        title = "Nível 3 — RPG 32-Bit (Imersivo)",
        badge = "32-BIT",
        description = "Vocabulário completo estilo Sea of Stars / Final Fantasy com botões (?) para traduzir qualquer termo.",
        previewTerms = "Limite da Jornada • Ouro em Mãos • Linha de Mana / HP",
        accent = SolarMagenta
    )
)

@Composable
fun ConfiguracoesScreen(
    state: LedgerUiState.Ready,
    audioSettings: AudioSettings,
    notificationSettings: NotificationSettings,
    onBack: () -> Unit,
    onSelectLanguageLevel: (Int) -> Unit,
    onSelectCycleStartDay: (Int) -> Unit,
    onToggleMasterMute: (Boolean) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onChangeSfxVolume: (Float) -> Unit,
    onToggleAmbience: (Boolean) -> Unit,
    onChangeAmbienceVolume: (Float) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectSoundProfile: (SoundProfile) -> Unit,
    onResetAudioDefaults: () -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onToggleGoalAlerts: (Boolean) -> Unit,
    onSelectReminderTime: (Int, Int) -> Unit,
    onNotificationFeedback: (String) -> Unit,
    onReplayTour: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile
    val currentLevel = profile.languageLevel
    val wisdomLevel = ((profile.xpPoints / 100L) + 1L).toInt()
    val xpIntoLevel = (profile.xpPoints % 100L).toFloat() / 100f
    val heroClass = safeEnumOf(profile.financialClass, FinancialClass.GUARDIAN)
    val haptics = LocalHapticFeedback.current
    var activeFilter by remember { mutableIntStateOf(0) } // 0 = Todos, 1 = Níveis, 2 = Áudio, 3 = Alertas

    fun maybeHaptic() {
        if (audioSettings.hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. 32-Bit Settings Top Header
            item {
                RpgPsxWindow(
                    borderColor = LunarCyan.copy(alpha = 0.55f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    maybeHaptic()
                                    RpgAudioEngine.playUiTap(audioSettings)
                                    onBack()
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("config_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = LunarPrimary
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            RpgIconMedallion(
                                icon = Icons.Default.Settings,
                                tint = LunarCyan,
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CONFIGURAÇÕES",
                                    style = CinzelTitleStyle.copy(fontSize = 16.sp, letterSpacing = 1.0.sp),
                                    color = LunarPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "NÍVEIS, ÁUDIO & ALERTAS RPG",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp
                                    ),
                                    color = LunarCyan.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        StatusChip(
                            text = if (audioSettings.masterMuted) "SOM MUDO" else "SOM ATIVO",
                            color = if (audioSettings.masterMuted) AmberWarn else EmeraldSafe
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Sub-filter Tabs: Todos | Níveis | Áudio | Alertas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightVoid)
                            .border(1.dp, BevelBorderIndigo, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Todos", "Níveis", "Áudio", "Alertas").forEachIndexed { idx, label ->
                            val selected = activeFilter == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selected) {
                                            Brush.horizontalGradient(
                                                listOf(LunarCyan.copy(alpha = 0.30f), CosmicIndigo.copy(alpha = 0.85f))
                                            )
                                        } else {
                                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                        }
                                    )
                                    .then(
                                        if (selected) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        maybeHaptic()
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        activeFilter = idx
                                    }
                                    .testTag("config_filter_$idx")
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (selected) LunarPrimary else OnSurfaceMuted,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // 2. ESCOLHA DOS NÍVEIS (Language Tone Levels + Hero Progression)
            if (activeFilter == 0 || activeFilter == 1) {
                item {
                    RpgPsxWindow(
                        borderColor = SolarMagenta.copy(alpha = 0.55f),
                        bracketColor = SolarMagenta,
                        contentPadding = PaddingValues(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                RpgIconMedallion(
                                    icon = Icons.Default.Tune,
                                    tint = SolarMagenta,
                                    size = 32.dp,
                                    iconSize = 17.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Escolha dos Níveis",
                                        style = CinzelTitleStyle.copy(fontSize = 15.sp),
                                        color = LunarPrimary
                                    )
                                    Text(
                                        text = "Complexidade de linguagem e imersão da interface",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceMuted
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            StatusChip(text = "Nível $currentLevel", color = LunarCyan)
                        }

                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            levelOptions.forEach { opt ->
                                val selected = currentLevel == opt.level
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            maybeHaptic()
                                            onSelectLanguageLevel(opt.level)
                                            RpgAudioEngine.playLevelSwitch(audioSettings, opt.level)
                                        }
                                        .testTag("config_level_${opt.level}"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) CosmicIndigo.copy(alpha = 0.88f) else MidnightVoid,
                                    border = BorderStroke(
                                        width = if (selected) 1.8.dp else 1.dp,
                                        color = if (selected) opt.accent else BevelBorderIndigo
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selecionado",
                                                        tint = opt.accent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                }
                                                Text(
                                                    text = opt.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (selected) LunarPrimary else OnSurfaceText
                                                )
                                            }
                                            StatusChip(text = opt.badge, color = opt.accent)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = opt.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        RpgInsetWell(
                                            borderColor = opt.accent.copy(alpha = 0.45f),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = opt.previewTerms,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 10.sp
                                                ),
                                                color = if (selected) opt.accent else OnSurfaceMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Hero Wisdom & Shield Rank Card
                        RpgInsetWell(
                            borderColor = GoldBadge.copy(alpha = 0.48f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${profile.heroName} • ${heroClass.titlePt}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GoldBadge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Sabedoria Nível $wisdomLevel (${profile.xpPoints} XP)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    ReserveShieldBadge(
                                        tier = state.stats.shieldTier,
                                        runwayMonths = state.stats.runwayMonths
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                RpgProgressBar(
                                    progress = xpIntoLevel,
                                    barColor = GoldBadge,
                                    secondaryColor = LunarCyan,
                                    height = 10.dp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${100L - (profile.xpPoints % 100L)} XP para Sabedoria Nível ${wisdomLevel + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceMuted
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Financial Cycle Start Day Selector (cycleStartDay 1..28)
                        val activeWindow = remember(profile.cycleStartDay) {
                            com.example.domain.AccountingEngine.getActiveCycleWindow(profile.cycleStartDay)
                        }
                        RpgInsetWell(
                            borderColor = LunarCyan.copy(alpha = 0.48f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Ciclo Financeiro (Dia do Salário)",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = LunarCyan
                                        )
                                        Text(
                                            text = "Janela ativa: ${activeWindow.label} (${activeWindow.elapsedDays}/${activeWindow.totalDays} dias)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                    }
                                    StatusChip(text = "Dia ${profile.cycleStartDay}", color = LunarCyan)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(1, 5, 10, 15, 20).forEach { presetDay ->
                                        val selected = profile.cycleStartDay == presetDay
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .clip(RoundedCornerShape(7.dp))
                                                .clickable {
                                                    maybeHaptic()
                                                    RpgAudioEngine.playUiTap(audioSettings)
                                                    onSelectCycleStartDay(presetDay)
                                                }
                                                .testTag("config_cycle_day_$presetDay"),
                                            shape = RoundedCornerShape(7.dp),
                                            color = if (selected) CosmicIndigo else MidnightVoid,
                                            border = BorderStroke(
                                                width = if (selected) 1.5.dp else 1.dp,
                                                color = if (selected) LunarCyan else BevelBorderIndigo
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Dia $presetDay",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (selected) LunarPrimary else OnSurfaceMuted,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. CONFIGURAÇÕES DE ÁUDIO (32-Bit Sound Mixer & Profiles)
            if (activeFilter == 0 || activeFilter == 2) {
                item {
                    RpgPsxWindow(
                        borderColor = LunarCyan.copy(alpha = 0.55f),
                        bracketColor = LunarCyan,
                        contentPadding = PaddingValues(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RpgIconMedallion(
                                    icon = if (audioSettings.masterMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    tint = if (audioSettings.masterMuted) AmberWarn else LunarCyan,
                                    size = 32.dp,
                                    iconSize = 18.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Configurações de Áudio",
                                        style = CinzelTitleStyle.copy(fontSize = 15.sp),
                                        color = LunarPrimary
                                    )
                                    Text(
                                        text = "Síntese 32-Bit em tempo real e resposta tátil",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceMuted
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Master Mute Row
                        RpgInsetWell(
                            borderColor = if (audioSettings.masterMuted) AmberWarn.copy(alpha = 0.5f) else LunarCyan.copy(alpha = 0.4f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Modo Silencioso Geral (Mudo)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (audioSettings.masterMuted) AmberWarn else OnSurfaceText
                                    )
                                    Text(
                                        text = "Desativa instantaneamente efeitos e prévia musical",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceMuted
                                    )
                                }
                                Switch(
                                    checked = audioSettings.masterMuted,
                                    onCheckedChange = { muted ->
                                        maybeHaptic()
                                        onToggleMasterMute(muted)
                                        if (!muted) {
                                            RpgAudioEngine.playUiTap(audioSettings.copy(masterMuted = false))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MidnightBase,
                                        checkedTrackColor = AmberWarn,
                                        uncheckedThumbColor = LunarCyan,
                                        uncheckedTrackColor = CosmicIndigo
                                    ),
                                    modifier = Modifier.testTag("audio_master_mute_switch")
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Sound Profile Selector
                        Text(
                            text = "TIMBRE DO MOTOR SONORO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = LunarCyan
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SoundProfile.entries.forEach { soundProf ->
                                val active = audioSettings.soundProfile == soundProf
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            maybeHaptic()
                                            onSelectSoundProfile(soundProf)
                                            RpgAudioEngine.playUiTap(audioSettings.copy(soundProfile = soundProf))
                                        }
                                        .testTag("sound_profile_${soundProf.name.lowercase()}"),
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (active) CosmicIndigo else MidnightVoid,
                                    border = BorderStroke(1.2.dp, if (active) LunarCyan else BevelBorderIndigo)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = if (active) LunarCyan else OnSurfaceMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = soundProf.label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (active) LunarPrimary else OnSurfaceText
                                            )
                                            Text(
                                                text = soundProf.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceMuted
                                            )
                                        }
                                        if (active) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(LunarCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // SFX Channel (Toggles, Volume Slider & Test Button)
                        AudioChannelCard(
                            title = "Efeitos Sonoros (SFX & Moedas)",
                            subtitle = "Cliques de menu, forja de lançamentos e troca de nível",
                            icon = Icons.Default.AutoAwesome,
                            accent = LunarCyan,
                            enabled = audioSettings.sfxEnabled && !audioSettings.masterMuted,
                            switchChecked = audioSettings.sfxEnabled,
                            volume = audioSettings.sfxVolume,
                            switchTag = "audio_sfx_switch",
                            sliderTag = "audio_sfx_slider",
                            testButtonText = "Testar SFX 32-Bit",
                            testButtonTag = "test_sfx_button",
                            onToggle = { enabled ->
                                maybeHaptic()
                                onToggleSfx(enabled)
                                if (enabled && !audioSettings.masterMuted) {
                                    RpgAudioEngine.playUiTap(audioSettings.copy(sfxEnabled = true))
                                }
                            },
                            onVolumeChange = onChangeSfxVolume,
                            onTestPlay = {
                                maybeHaptic()
                                RpgAudioEngine.playSuccessChime(audioSettings)
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        // Ambience Channel (Toggles, Volume Slider & Preview Button)
                        AudioChannelCard(
                            title = "Ambiência Astral (Trilha & Acordes)",
                            subtitle = "Arpejos crepusculares ao abrir o Oráculo ou recompensas",
                            icon = Icons.Default.MusicNote,
                            accent = SolarMagenta,
                            enabled = audioSettings.ambienceEnabled && !audioSettings.masterMuted,
                            switchChecked = audioSettings.ambienceEnabled,
                            volume = audioSettings.ambienceVolume,
                            switchTag = "audio_ambience_switch",
                            sliderTag = "audio_ambience_slider",
                            testButtonText = "Ouvir Prévia Astral",
                            testButtonTag = "test_ambience_button",
                            onToggle = { enabled ->
                                maybeHaptic()
                                onToggleAmbience(enabled)
                            },
                            onVolumeChange = onChangeAmbienceVolume,
                            onTestPlay = {
                                maybeHaptic()
                                RpgAudioEngine.playAmbiencePreview(audioSettings)
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        // Haptic Feedback Toggle
                        RpgInsetWell(
                            borderColor = BevelBorderIndigo,
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
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = EmeraldSafe,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Feedback Háptico (Vibração Tátil)",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurfaceText
                                        )
                                        Text(
                                            text = "Pulso sutil ao confirmar comandos na interface",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                    }
                                }
                                Switch(
                                    checked = audioSettings.hapticsEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        onToggleHaptics(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MidnightBase,
                                        checkedTrackColor = EmeraldSafe,
                                        uncheckedThumbColor = OnSurfaceMuted,
                                        uncheckedTrackColor = MidnightBase
                                    ),
                                    modifier = Modifier.testTag("audio_haptics_switch")
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clickable {
                                    maybeHaptic()
                                    onResetAudioDefaults()
                                }
                                .testTag("reset_audio_button"),
                            shape = RoundedCornerShape(7.dp),
                            color = MidnightVoid,
                            border = BorderStroke(1.dp, BevelBorderIndigo)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Restaurar Padrões de Áudio",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LunarPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3b. ARAUTO & NOTIFICAÇÕES PUSH GAMIFICADAS
            if (activeFilter == 0 || activeFilter == 3) {
                item {
                    NotificationSettingsCard(
                        state = state,
                        audioSettings = audioSettings,
                        notificationSettings = notificationSettings,
                        onToggleReminders = onToggleReminders,
                        onToggleGoalAlerts = onToggleGoalAlerts,
                        onSelectReminderTime = onSelectReminderTime,
                        onFeedbackMessage = onNotificationFeedback
                    )
                }
            }

            // 4. Tour & Account Utilities
            item {
                RpgPsxWindow(
                    borderColor = BevelBorderIndigo,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "Jornada & Sessão",
                        style = CinzelTitleStyle.copy(fontSize = 14.sp),
                        color = LunarPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    LunarCoreButton(
                        text = "Rever Tour Primeira Missão",
                        onClick = {
                            maybeHaptic()
                            RpgAudioEngine.playUiTap(audioSettings)
                            onReplayTour()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "config_replay_tour_button"
                    )
                    Spacer(Modifier.height(8.dp))
                    SolarCompanionButton(
                        text = "Sair da Conta",
                        onClick = {
                            maybeHaptic()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "config_sign_out_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioChannelCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    enabled: Boolean,
    switchChecked: Boolean,
    volume: Float,
    switchTag: String,
    sliderTag: String,
    testButtonText: String,
    testButtonTag: String,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onTestPlay: () -> Unit
) {
    val pct = (volume * 100f).roundToInt()
    RpgInsetWell(
        borderColor = if (enabled) accent.copy(alpha = 0.55f) else BevelBorderIndigo,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(icon, contentDescription = null, tint = if (enabled) accent else OnSurfaceMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (enabled) OnSurfaceText else OnSurfaceMuted
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
                Switch(
                    checked = switchChecked,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MidnightBase,
                        checkedTrackColor = accent,
                        uncheckedThumbColor = OnSurfaceMuted,
                        uncheckedTrackColor = MidnightBase
                    ),
                    modifier = Modifier.testTag(switchTag)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    enabled = enabled,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = MidnightBase,
                        disabledThumbColor = OnSurfaceMuted,
                        disabledActiveTrackColor = BevelBorderIndigo
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(sliderTag)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (enabled) accent else OnSurfaceMuted
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable(enabled = enabled, onClick = onTestPlay)
                    .testTag(testButtonTag),
                shape = RoundedCornerShape(6.dp),
                color = if (enabled) accent.copy(alpha = 0.14f) else MidnightBase,
                border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.55f) else BevelBorderIndigo)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (enabled) accent else OnSurfaceMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (enabled) testButtonText else "Canal Silenciado",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (enabled) accent else OnSurfaceMuted
                    )
                }
            }
        }
    }
}
