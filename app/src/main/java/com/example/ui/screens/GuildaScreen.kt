package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioSettings
import com.example.audio.RpgAudioEngine
import com.example.data.BudgetBucket
import com.example.data.DateUtils
import com.example.data.FinancialClass
import com.example.data.GuildSplitMode
import com.example.data.PrivacyMode
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import com.example.domain.AvatarEvolutionEngine
import com.example.domain.TermKey
import com.example.ui.components.HeroEvolutionAltarSection
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.PixelHeroAvatar
import com.example.ui.components.ReserveShieldBadge
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.RpgIconMedallion
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgProgressBar
import com.example.ui.components.RpgPsxWindow
import com.example.ui.components.SolarCompanionButton
import com.example.ui.components.StatusChip
import com.example.ui.components.TermLabelWithTooltip
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CrimsonRisk
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
import com.example.ui.theme.SolarSecondary
import com.example.ui.theme.StatCurrencyCompact
import com.example.ui.theme.StatCurrencySmall
import com.example.ui.theme.TertiaryIndigo
import com.example.viewmodel.LedgerUiState
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuildaScreen(
    state: LedgerUiState.Ready,
    isSubmitting: Boolean,
    audioSettings: AudioSettings = AudioSettings(),
    onUpdateGuildSettings: (PrivacyMode, GuildSplitMode) -> Unit,
    onSettleGuild: () -> Unit,
    onJoinGuildCode: (String) -> Unit,
    onAskOracle: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = state.profile.languageLevel
    val profile = state.profile
    val stats = state.stats
    val guild = state.guild
    val settlement = state.guildSettlement

    val currentPrivacy = safeEnumOf(profile.privacyMode, PrivacyMode.CATEGORY_SUMMARY)
    val currentSplit = safeEnumOf(guild?.splitMode, GuildSplitMode.PROPORTIONAL)
    val heroClass = safeEnumOf(profile.financialClass, FinancialClass.GUARDIAN)
    val evolution = remember(profile) { AvatarEvolutionEngine.compute(profile) }
    val wisdomLevel = evolution.wisdomLevel
    val xpInLevel = evolution.xpInCurrentLevel

    val trackedCategories = state.categoryStatuses.filter { it.capCents > 0L }
    val withinCapCount = trackedCategories.count { it.ratio <= 1.0f }
    val overCapCount = trackedCategories.size - withinCapCount
    val disciplinePct = if (trackedCategories.isNotEmpty()) {
        ((withinCapCount.toFloat() / trackedCategories.size.toFloat()) * 100f).roundToInt()
    } else 100

    val incomeCents = profile.monthlyNetIncomeCents.coerceAtLeast(100_00L)
    val essentialMonthlyCost = remember(state.budgets, incomeCents) {
        state.budgets
            .filter { safeEnumOf(it.bucket, BudgetBucket.ESSENTIAL) == BudgetBucket.ESSENTIAL }
            .sumOf { ((incomeCents * it.percentageCap) / 100.0).roundToLong() }
            .coerceAtLeast(100_000L)
    }
    val sixMonthTargetCents = essentialMonthlyCost * 6L
    val fortressProgress = (profile.emergencyReserveCents.toFloat() / sixMonthTargetCents.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val fortressPct = (fortressProgress * 100f).roundToInt()
    val fortressMissing = (sixMonthTargetCents - profile.emergencyReserveCents).coerceAtLeast(0L)

    var inviteCodeInput by remember { mutableStateOf("") }

    val partnerDisplay = settlement?.partnerBName ?: guild?.companionDemoName ?: "Parceiro(a)"
    val heroPairTitle = if (guild != null) "${profile.heroName} & $partnerDisplay" else profile.heroName

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Title + Settings Button
            item {
                Column {
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
                                icon = Icons.Default.Shield,
                                tint = SolarMagenta,
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            TermLabelWithTooltip(
                                termKey = TermKey.GUILDA_VAULT,
                                languageLevel = lang,
                                textColor = LunarPrimary,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            onClick = {
                                RpgAudioEngine.playUiTap(audioSettings)
                                onOpenSettings()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("guilda_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = LunarCyan)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ficha do Guardião 32-Bit, atributos vitais, relíquias e cofre compartilhado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
            }

            // ================= 1. FICHA DE HERÓI & ALIANÇA 32-BIT =================
            item {
                RpgPsxWindow(
                    borderColor = LunarCyan.copy(alpha = 0.60f),
                    bracketColor = LunarCyan,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "FICHA DE HERÓI",
                                style = CinzelTitleStyle.copy(fontSize = 13.sp),
                                color = LunarPrimary
                            )
                            StatusChip(text = "32-BIT", color = TertiaryIndigo)
                        }
                        StatusChip(
                            text = if (guild != null) "GUILDA VINCULADA" else "JORNADA SOLO",
                            color = if (guild != null) EmeraldSafe else LunarCyan
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.4f), contentPadding = PaddingValues(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dual overlapping P1 / P2 hero ring portrait
                            Box(modifier = Modifier.width(if (guild != null) 92.dp else 64.dp)) {
                                if (guild != null) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(SolarMagenta, CosmicIndigo)))
                                            .border(2.dp, SolarSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = partnerDisplay.take(2).uppercase(),
                                            style = CinzelTitleStyle.copy(fontSize = 15.sp),
                                            color = Color.White
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SolarMagenta)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "P2",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                                    PixelHeroAvatar(
                                        hairIndex = profile.avatarHair,
                                        outfitIndex = profile.avatarOutfit,
                                        auraKey = profile.avatarAura,
                                        size = 58.dp,
                                        wisdomLevel = evolution.wisdomLevel,
                                        netWorthCents = evolution.netWorthCents
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LunarCyan)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "P1",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = MidnightBase
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = heroPairTitle,
                                        style = CinzelTitleStyle.copy(fontSize = 15.sp),
                                        color = LunarPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    StatusChip(text = "NVL $wisdomLevel", color = GoldBadge)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${heroClass.titlePt} (${heroClass.essentialPct}/${heroClass.lifestylePct}/${heroClass.reservePct})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = SolarMagenta, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = if (guild != null) "Pacto: ${guild.guildName}" else "Sem pacto vinculado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SolarSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ATB Experiencia Astral Gauge
                    RpgInsetWell(borderColor = BevelBorderIndigo, contentPadding = PaddingValues(10.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldBadge, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "EXPERIÊNCIA (XP)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TertiaryIndigo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$xpInLevel/100 • Próx. Nv.${wisdomLevel + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LunarPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            RpgProgressBar(
                                progress = xpInLevel.toFloat() / 100f,
                                barColor = LunarCyan,
                                secondaryColor = TertiaryIndigo,
                                height = 12.dp
                            )
                        }
                    }
                }
            }

            // ================= 1B. ALTAR DE EVOLUÇÃO, TRAJES & MASCOTES =================
            item {
                HeroEvolutionAltarSection(
                    profile = profile,
                    evolution = evolution
                )
            }

            // ================= 2. ATRIBUTOS DE SABEDORIA FINANCEIRA (2x2) =================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atributos Financeiros",
                            style = CinzelTitleStyle,
                            color = LunarPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        ReserveShieldBadge(tier = stats.shieldTier, runwayMonths = stats.runwayMonths)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GuardianAttributeCard(
                            code = "DEF • VIGOR",
                            icon = Icons.Default.Shield,
                            accent = EmeraldSafe,
                            value = "${String.format(Locale.US, "%.1f", stats.runwayMonths)} Meses",
                            subtitle = "Runway Reserva",
                            statusLabel = if (stats.runwayMonths >= 4.0) "Inquebrável" else "Em Forja",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        GuardianAttributeCard(
                            code = "ATK • DISCIPLINA",
                            icon = Icons.Default.WbSunny,
                            accent = SolarMagenta,
                            value = "$disciplinePct%",
                            subtitle = "Categorias no Teto",
                            statusLabel = if (overCapCount == 0) "0 Estouros" else "$overCapCount Alerta(s)",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GuardianAttributeCard(
                            code = "SPD • FLUXO",
                            icon = Icons.Default.Speed,
                            accent = LunarCyan,
                            value = "${formatBrl(stats.safeToSpendDailyCents)}/d",
                            subtitle = "Safe-to-Spend Hoje",
                            statusLabel = if (stats.safeToSpendDailyCents > 0L) "Fluxo Ativo" else "Crítico",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        GuardianAttributeCard(
                            code = "INT • VISÃO",
                            icon = Icons.Default.AutoFixHigh,
                            accent = if (stats.nextMonthCommitmentPct > 55) AmberWarn else TertiaryIndigo,
                            value = "${stats.nextMonthCommitmentPct}% Próx.",
                            subtitle = "Renda Pré-Ocupada",
                            statusLabel = if (stats.nextMonthCommitmentPct <= 50) "Blindado" else "Monitorar",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // ================= 3. GRIMÓRIO DE HABILIDADES PASSIVAS =================
            item {
                val classSkillTitle = when (heroClass) {
                    FinancialClass.GUARDIAN -> "Escudo Inquebrável"
                    FinancialClass.EXPLORER -> "Bússola de Fluxo"
                    FinancialClass.ALCHEMIST -> "Transmutação Fiscal"
                }
                val classSkillDesc = when (heroClass) {
                    FinancialClass.GUARDIAN -> "Prioriza ${heroClass.reservePct}% em reservas para elevar seu Runway e desbloquear bônus de XP."
                    FinancialClass.EXPLORER -> "Mantém ${heroClass.lifestylePct}% para experiências sem romper a Linha de Equilíbrio."
                    FinancialClass.ALCHEMIST -> "Direciona ${heroClass.reservePct}% para quitar parcelas e zerar comprometimento futuro."
                }
                val antiEmergUnlocked = wisdomLevel >= 4

                RpgBevelCard(borderColor = LunarCyan.copy(alpha = 0.50f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Grimório de Habilidades", style = CinzelTitleStyle, color = LunarPrimary)
                        }
                        Text(
                            text = if (antiEmergUnlocked) "3 ATIVAS" else "2 ATIVAS • 1 BLOQ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LunarCyan
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    SkillPassiveRow(
                        icon = Icons.Default.Security,
                        title = classSkillTitle,
                        badge = "CLASSE ${heroClass.titlePt.uppercase()}",
                        description = classSkillDesc,
                        accent = LunarCyan,
                        unlocked = true
                    )
                    Spacer(Modifier.height(8.dp))
                    SkillPassiveRow(
                        icon = Icons.Default.Hub,
                        title = "Sinergia Astral",
                        badge = "GUILDA",
                        description = "Rateio transparente e acerto neutro sem distorcer tetos individuais dos parceiros.",
                        accent = SolarMagenta,
                        unlocked = true
                    )
                    Spacer(Modifier.height(8.dp))
                    SkillPassiveRow(
                        icon = if (antiEmergUnlocked) Icons.Default.Verified else Icons.Default.Lock,
                        title = "Filtro Anti-Imprevisto",
                        badge = if (antiEmergUnlocked) "DESBLOQUEADA" else "NVL. 4 REQUERIDO",
                        description = "Recalcula automaticamente seu Limite Seguro Diário para proteger metas em emergências.",
                        accent = if (antiEmergUnlocked) EmeraldSafe else OnSurfaceMuted,
                        unlocked = antiEmergUnlocked
                    )
                }
            }

            // ================= 4. RELÍQUIAS & HONRARIAS (SALA DE TROFÉUS) =================
            item {
                val conquestUnlocked = state.breakEven.isInConquestZone
                val serenityUnlocked = stats.runwayMonths >= 3.0

                RpgBevelCard(borderColor = GoldBadge.copy(alpha = 0.50f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldBadge, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Relíquias & Honrarias", style = CinzelTitleStyle, color = LunarPrimary)
                        }
                        Text("SALA DE TROFÉUS", style = MaterialTheme.typography.labelSmall, color = TertiaryIndigo)
                    }

                    Spacer(Modifier.height(10.dp))
                    RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.45f), contentPadding = PaddingValues(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RpgIconMedallion(icon = Icons.Default.AutoAwesome, tint = GoldBadge, size = 38.dp, iconSize = 20.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("RELÍQUIA EQUIPADA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = GoldBadge)
                                Text("Amuleto do Solstício • Escudo Nv.${stats.shieldTier}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceText)
                                Text("Protege suas metas ajustando o teto diário em tempo real.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrophyTile(
                            title = "Ponto de Equilíbrio",
                            detail = if (conquestUnlocked) "Margem ${formatBrl(state.breakEven.surplusOrDeficitCents)}" else "Em busca da Zona Verde",
                            unlocked = conquestUnlocked,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        TrophyTile(
                            title = "Escudo da Serenidade",
                            detail = if (serenityUnlocked) "3+ Meses Concluídos" else "Atual: ${String.format(Locale.US, "%.1f", stats.runwayMonths)}m / 3.0m",
                            unlocked = serenityUnlocked,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    RpgInsetWell(borderColor = BevelBorderIndigo, contentPadding = PaddingValues(10.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fortaleza 6m (${formatBrl(sixMonthTargetCents)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceText,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$fortressPct% Concluído",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LunarPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            RpgProgressBar(
                                progress = fortressProgress,
                                barColor = LunarCyan,
                                secondaryColor = EmeraldSafe,
                                height = 10.dp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (fortressMissing > 0L) "Faltam ${formatBrl(fortressMissing)} para erguer a fortaleza definitiva." else "Fortaleza definitiva concluída!",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }
            }

            // ================= 5. COFRE COMPARTILHADO & MEMBROS DA ALIANÇA =================
            item {
                RpgPsxWindow(
                    borderColor = SolarMagenta.copy(alpha = 0.65f),
                    bracketColor = SolarMagenta
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, SolarMagenta.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    ) {
                        com.example.ui.components.SampledDrawableImage(
                            resId = R.drawable.img_guild_crest_1794969168910,
                            contentDescription = "Brasão da Guilda",
                            maxDimPx = 640,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MidnightVoid.copy(alpha = 0.90f),
                                            MidnightVoid.copy(alpha = 0.50f),
                                            MidnightVoid.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = SolarMagenta, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = guild?.guildName ?: "Cofre da Guilda",
                                    style = CinzelTitleStyle,
                                    color = SolarSecondary
                                )
                            }
                            if (guild != null) {
                                StatusChip(text = "Convite: ${guild.inviteCode}", color = LunarCyan)
                            }
                        }
                    }

                    if (guild != null && settlement != null) {
                        Spacer(Modifier.height(14.dp))
                        Text("Membros da Aliança & Acerto (${currentSplit.labelPt})", style = MaterialTheme.typography.titleSmall, color = LunarPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RpgInsetWell(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                borderColor = LunarCyan.copy(alpha = 0.50f),
                                contentPadding = PaddingValues(11.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(
                                            text = settlement.partnerAName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = LunarCyan,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        StatusChip(text = "LÍDER", color = LunarCyan)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Column {
                                        Text(formatBrl(settlement.partnerAPaidCents), style = StatCurrencyCompact, color = OnSurfaceText, maxLines = 1, softWrap = false)
                                        Text("Cota justa: ${(settlement.partnerAShareRatio * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                                    }
                                }
                            }
                            RpgInsetWell(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                borderColor = SolarMagenta.copy(alpha = 0.50f),
                                contentPadding = PaddingValues(11.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(
                                            text = settlement.partnerBName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SolarMagenta,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        StatusChip(text = "ALIADO", color = SolarMagenta)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Column {
                                        Text(formatBrl(settlement.partnerBPaidCents), style = StatCurrencyCompact, color = OnSurfaceText, maxLines = 1, softWrap = false)
                                        Text("Cota justa: ${(settlement.partnerBShareRatio * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        if (settlement.settlementAmountCents > 0L) {
                            Text(
                                text = "${settlement.debtorName} precisa transferir ${formatBrl(settlement.settlementAmountCents)} para ${settlement.creditorName} para equilibrar o mês.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmberWarn
                            )
                            Spacer(Modifier.height(10.dp))
                            SolarCompanionButton(
                                text = if (isSubmitting) "Registrando..." else "Acertar Mês (${formatBrl(settlement.settlementAmountCents)})",
                                enabled = !isSubmitting,
                                onClick = {
                                    RpgAudioEngine.playSuccessChime(audioSettings)
                                    onSettleGuild()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settle_guild_button"
                            )
                        } else {
                            StatusChip(text = "Tudo quitado e equilibrado neste mês!", color = EmeraldSafe)
                        }

                        Spacer(Modifier.height(14.dp))
                        Text("Meta Compartilhada: ${guild.sharedGoalName}", style = MaterialTheme.typography.labelMedium, color = OnSurfaceText)
                        Spacer(Modifier.height(4.dp))
                        val goalRatio = if (guild.sharedGoalTargetCents > 0L) {
                            guild.sharedGoalSavedCents.toFloat() / guild.sharedGoalTargetCents.toFloat()
                        } else 0f
                        RpgProgressBar(progress = goalRatio, barColor = EmeraldSafe, secondaryColor = LunarCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${formatBrl(guild.sharedGoalSavedCents)} de ${formatBrl(guild.sharedGoalTargetCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }

            if (state.guildEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "Lançamentos Compartilhados (${state.guildEntries.size})",
                        style = CinzelTitleStyle,
                        color = LunarPrimary
                    )
                }
                items(state.guildEntries.take(8), key = { it.id }) { entry ->
                    RpgInsetWell(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SolarMagenta.copy(alpha = 0.4f),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurfaceText)
                                Text(
                                    text = "${entry.authorName} • ${DateUtils.epochDayToDisplay(entry.dateEpochDay)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceMuted
                                )
                            }
                            Text(
                                text = formatBrl(entry.amountCents),
                                style = StatCurrencySmall,
                                color = SolarSecondary
                            )
                        }
                    }
                }
            }

            // Pact Settings + Privacy + Join by Code
            item {
                RpgBevelCard {
                    Text("Modo de Rateio Justo", style = CinzelTitleStyle, color = LunarPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GuildSplitMode.entries.forEach { mode ->
                            val active = currentSplit == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onUpdateGuildSettings(currentPrivacy, mode)
                                    }
                                    .testTag("split_${mode.name.lowercase()}"),
                                shape = RoundedCornerShape(6.dp),
                                color = if (active) CosmicIndigo else MidnightVoid,
                                border = BorderStroke(1.5.dp, if (active) SolarMagenta else BevelBorderIndigo)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = mode.labelPt,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (active) SolarSecondary else OnSurfaceMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Privacidade do Seu Inventário Solo", style = CinzelTitleStyle, color = LunarPrimary)
                    Spacer(Modifier.height(6.dp))
                    PrivacyMode.entries.forEach { pm ->
                        val active = currentPrivacy == pm
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    RpgAudioEngine.playUiTap(audioSettings)
                                    onUpdateGuildSettings(pm, currentSplit)
                                }
                                .testTag("privacy_${pm.name.lowercase()}"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (active) CosmicIndigo else MidnightVoid,
                            border = BorderStroke(1.dp, if (active) LunarCyan else BevelBorderIndigo)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(pm.labelPt, style = MaterialTheme.typography.labelMedium, color = if (active) LunarPrimary else OnSurfaceText)
                                Text(pm.descPt, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Entrar em outra Guilda por Código", style = CinzelTitleStyle, color = LunarPrimary)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inviteCodeInput,
                            onValueChange = { if (it.length <= 12) inviteCodeInput = it.uppercase() },
                            placeholder = { Text("Ex: SOL123") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LunarCyan,
                                unfocusedBorderColor = BevelBorderIndigo,
                                focusedContainerColor = MidnightVoid,
                                unfocusedContainerColor = MidnightVoid,
                                focusedTextColor = OnSurfaceText,
                                unfocusedTextColor = OnSurfaceText
                            ),
                            modifier = Modifier.weight(1f).testTag("guild_invite_input")
                        )
                        LunarCoreButton(
                            text = "Entrar",
                            enabled = inviteCodeInput.trim().length >= 4 && !isSubmitting,
                            onClick = {
                                RpgAudioEngine.playUiTap(audioSettings)
                                onJoinGuildCode(inviteCodeInput)
                            },
                            testTag = "join_guild_button"
                        )
                    }
                }
            }

            // ================= 6. CONSELHO DO ORÁCULO & CERTIFICADO DA JORNADA =================
            item {
                RpgBevelCard(borderColor = LunarCyan.copy(alpha = 0.55f)) {
                    RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.35f), contentPadding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Conselho do Oráculo para a Aliança", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = LunarPrimary)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (settlement != null && settlement.settlementAmountCents > 0L) {
                                        "Um acerto neutro de ${formatBrl(settlement.settlementAmountCents)} de ${settlement.debtorName} para ${settlement.creditorName} equilibra o mês sem distorcer seus tetos."
                                    } else {
                                        "Sua reserva garante ${String.format(Locale.US, "%.1f", stats.runwayMonths)} meses de fôlego com $disciplinePct% das categorias dentro da meta."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceText
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    LunarCoreButton(
                        text = "Consultar Oráculo sobre a Guilda",
                        onClick = {
                            RpgAudioEngine.playUiTap(audioSettings)
                            onAskOracle(
                                "Analise o perfil da minha Guilda: Herói ${profile.heroName} (${heroClass.titlePt}), Runway ${String.format(Locale.US, "%.1f", stats.runwayMonths)} meses, Disciplina $disciplinePct%, Comprometimento próximo mês ${stats.nextMonthCommitmentPct}%. Como podemos evoluir juntos neste mês?"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "guild_ask_oracle_button"
                    )

                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable {
                                RpgAudioEngine.playSuccessChime(audioSettings)
                                val report = buildString {
                                    appendLine("=== CERTIFICADO SOLSTICE LEDGER ===")
                                    appendLine("Guardião: $heroPairTitle | Nível Sabedoria: $wisdomLevel (${profile.xpPoints} XP)")
                                    appendLine("Classe: ${heroClass.titlePt} (${heroClass.essentialPct}/${heroClass.lifestylePct}/${heroClass.reservePct})")
                                    appendLine("Saldo em Conta: ${formatBrl(profile.checkingBalanceCents)}")
                                    appendLine("Reserva / Runway: ${formatBrl(profile.emergencyReserveCents)} (${String.format(Locale.US, "%.1f", stats.runwayMonths)} meses)")
                                    appendLine("Safe-to-Spend Diário: ${formatBrl(stats.safeToSpendDailyCents)}/dia")
                                    appendLine("Comprometimento Próx. Mês: ${stats.nextMonthCommitmentPct}%")
                                    if (guild != null) {
                                        appendLine("Guilda: ${guild.guildName} (Código: ${guild.inviteCode})")
                                    }
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Certificado Solstice Ledger - ${profile.heroName}")
                                    putExtra(Intent.EXTRA_TEXT, report)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Exportar Certificado da Jornada"))
                            }
                            .testTag("export_journey_report_button"),
                        shape = RoundedCornerShape(8.dp),
                        color = MidnightVoid,
                        border = BorderStroke(1.dp, TertiaryIndigo.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = TertiaryIndigo, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "EXPORTAR CERTIFICADO DA JORNADA",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TertiaryIndigo
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuardianAttributeCard(
    code: String,
    icon: ImageVector,
    accent: Color,
    value: String,
    subtitle: String,
    statusLabel: String,
    modifier: Modifier = Modifier
) {
    RpgBevelCard(
        modifier = modifier,
        borderColor = accent.copy(alpha = 0.45f),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(value, style = StatCurrencyCompact, color = LunarPrimary, maxLines = 1, softWrap = false)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(MidnightVoid)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = OnSurfaceMuted)
                    Text(statusLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = accent, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun SkillPassiveRow(
    icon: ImageVector,
    title: String,
    badge: String,
    description: String,
    accent: Color,
    unlocked: Boolean
) {
    RpgInsetWell(
        borderColor = if (unlocked) accent.copy(alpha = 0.45f) else BevelBorderIndigo,
        contentPadding = PaddingValues(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            RpgIconMedallion(
                icon = icon,
                tint = accent,
                size = 34.dp,
                iconSize = 18.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (unlocked) OnSurfaceText else OnSurfaceMuted,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(text = badge, color = accent)
                }
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun TrophyTile(
    title: String,
    detail: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    RpgInsetWell(
        modifier = modifier,
        borderColor = if (unlocked) EmeraldSafe.copy(alpha = 0.55f) else BevelBorderIndigo,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    imageVector = if (unlocked) Icons.Default.Verified else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (unlocked) EmeraldSafe else OnSurfaceMuted,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (unlocked) LunarPrimary else OnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(detail, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
        }
    }
}
