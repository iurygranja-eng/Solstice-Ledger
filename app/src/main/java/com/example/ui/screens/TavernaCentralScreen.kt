package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioSettings
import com.example.data.DateUtils
import com.example.data.PaymentMethod
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.ViewMode
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import com.example.domain.AccountingEngine
import com.example.domain.LanguageDictionary
import com.example.domain.MissionSeverity
import com.example.domain.TermKey
import com.example.ui.components.ClickableHelpBadge
import com.example.ui.components.ExplainerContent
import com.example.ui.components.ReserveShieldBadge
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.RpgIconMedallion
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgProgressBar
import com.example.ui.components.RpgPsxWindow
import com.example.ui.components.StatusChip
import com.example.ui.components.TavernaDynamicAvatarCluster
import com.example.ui.components.TavernaLoadoutBadgeStrip
import com.example.ui.components.TavernaWisdomLoadoutDialog
import com.example.ui.components.TermLabelWithTooltip
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AstralSurface
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CrimsonRisk
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary
import com.example.ui.theme.StatCurrencyCompact
import com.example.ui.theme.StatCurrencyLarge
import com.example.ui.theme.StatCurrencyMedium
import com.example.ui.theme.StatCurrencySmall
import com.example.viewmodel.LedgerUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TavernaCentralScreen(
    state: LedgerUiState.Ready,
    viewMode: ViewMode,
    audioSettings: AudioSettings,
    onSetViewMode: (ViewMode) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExplainer: (ExplainerContent) -> Unit,
    onAskOracleDirect: (String) -> Unit,
    onPayOpenBill: () -> Unit,
    onReplayTour: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile
    val lang = profile.languageLevel
    val activeCycleWindow = remember(profile.cycleStartDay) {
        AccountingEngine.getActiveCycleWindow(profile.cycleStartDay)
    }
    val dayProgress = activeCycleWindow.progressRatio.toFloat()
    val wisdomLevel = ((profile.xpPoints / 100L) + 1L).toInt()
    var showWisdomLoadoutDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header HUD (32-Bit Solstice Crest, Dynamic Avatar Loadout & Tone/Mode Switches)
            item {
                RpgPsxWindow(
                    borderColor = if (viewMode == ViewMode.GUILDA) SolarMagenta.copy(alpha = 0.65f) else LunarCyan.copy(alpha = 0.45f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    // Band 1: Dynamic Hero Avatar (Aura + Costume + Accessory + Mascot) + Identity + Action Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TavernaDynamicAvatarCluster(
                            profile = profile,
                            wisdomLevel = wisdomLevel,
                            onClick = { showWisdomLoadoutDialog = true }
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "SOLSTICE",
                                    style = CinzelTitleStyle.copy(
                                        fontSize = 14.sp,
                                        letterSpacing = 1.4.sp
                                    ),
                                    color = LunarPrimary,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoldBadge.copy(alpha = 0.16f))
                                        .border(1.dp, GoldBadge.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "32-BIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GoldBadge,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile.heroName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = OnSurfaceText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(LunarCyan)
                                )
                                Text(
                                    text = "Nv. $wisdomLevel",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldBadge,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = onReplayTour,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("replay_tour_button")
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = "Rever Tour Primeira Missão", tint = LunarPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair da conta", tint = OnSurfaceMuted, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Band 1B: Dynamic Wisdom Equipment Strip (Clothing, Accessory & Mascot)
                    TavernaLoadoutBadgeStrip(
                        profile = profile,
                        onClick = { showWisdomLoadoutDialog = true }
                    )

                    Spacer(Modifier.height(10.dp))

                    // Band 2: Reserve Shield Tier + Solo/Guilda Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReserveShieldBadge(
                            tier = state.stats.shieldTier,
                            runwayMonths = state.stats.runwayMonths
                        )
                        Spacer(Modifier.width(8.dp))
                        SoloGuildSwitch(
                            viewMode = viewMode,
                            onSelect = onSetViewMode
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Band 3: Active Language Level + Audio Status / Settings Bar
                    val levelLabel = when (lang) {
                        1 -> "1. Fácil"
                        2 -> "2. Médio"
                        else -> "3. RPG 32-Bit"
                    }
                    val soundActive = !audioSettings.masterMuted && (audioSettings.sfxEnabled || audioSettings.ambienceEnabled)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF060A17))
                                .border(1.dp, LunarCyan.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
                                .clickable(onClick = onOpenSettings)
                                .testTag("header_level_pill")
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nível: $levelLabel",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = LunarPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = if (soundActive) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (soundActive) "Áudio Ativo" else "Áudio Silenciado",
                                tint = if (soundActive) EmeraldSafe else AmberWarn,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(LunarCyan.copy(alpha = 0.24f), CosmicIndigo.copy(alpha = 0.85f))
                                    )
                                )
                                .border(1.dp, LunarCyan.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                .clickable(onClick = onOpenSettings)
                                .testTag("open_settings_button")
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Abrir Configurações",
                                tint = LunarPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Config",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = LunarPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 2. Guilda Summary Banner when in GUILDA mode
            if (viewMode == ViewMode.GUILDA && state.guildSettlement != null) {
                item {
                    val gs = state.guildSettlement
                    RpgBevelCard(
                        borderColor = SolarMagenta.copy(alpha = 0.7f),
                        containerColor = CosmicIndigo.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TermLabelWithTooltip(
                                termKey = TermKey.GUILDA_VAULT,
                                languageLevel = lang,
                                textColor = SolarSecondary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            StatusChip(text = state.guild?.guildName ?: "Guilda", color = SolarMagenta)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Text(gs.partnerAName, style = MaterialTheme.typography.labelSmall, color = LunarCyan, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatBrl(gs.partnerAPaidCents), style = StatCurrencyCompact, color = OnSurfaceText, maxLines = 1, softWrap = false)
                                Text("${(gs.partnerAShareRatio * 100).roundToInt()}% do rateio", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            }
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Text(gs.partnerBName, style = MaterialTheme.typography.labelSmall, color = SolarMagenta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatBrl(gs.partnerBPaidCents), style = StatCurrencyCompact, color = OnSurfaceText, maxLines = 1, softWrap = false)
                                Text("${(gs.partnerBShareRatio * 100).roundToInt()}% do rateio", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (gs.settlementAmountCents > 0L)
                                "Acerto do Mês: ${gs.debtorName} transfere ${formatBrl(gs.settlementAmountCents)} para ${gs.creditorName} (veja em Mapa das Estrelas)."
                            else
                                "Guilda 100% equilibrada neste mês!",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSafe
                        )
                    }
                }
            }

            // 3. Hero Card: Limite Seguro Diário + Caixa vs Cartão + Ponto de Equilíbrio
            item {
                val safeToSpend = state.stats.safeToSpendDailyCents
                val safeColor = when {
                    safeToSpend <= 0L -> CrimsonRisk
                    safeToSpend < 4_500L -> AmberWarn
                    else -> EmeraldSafe
                }

                RpgBevelCard(borderColor = LunarCyan.copy(alpha = 0.72f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, BevelBorderIndigo, RoundedCornerShape(6.dp))
                    ) {
                        com.example.ui.components.SampledDrawableImage(
                            resId = R.drawable.img_tavern_banner_1794969168435,
                            contentDescription = null,
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
                                            AstralSurface.copy(alpha = 0.92f),
                                            AstralSurface.copy(alpha = 0.55f),
                                            MidnightVoid.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TermLabelWithTooltip(
                                termKey = TermKey.SAFE_TO_SPEND,
                                languageLevel = lang,
                                textColor = LunarPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            ClickableHelpBadge(
                                contentDescription = "Entender Limite Seguro Diário",
                                testTag = "help_safe_to_spend",
                                onClick = {
                                    onOpenExplainer(
                                        ExplainerContent(
                                            title = LanguageDictionary.get(TermKey.SAFE_TO_SPEND, lang).label,
                                            whatItShows = "Divide o que sobra da sua renda mensal (já descontando custos fixos, gastos do mês e sua meta de reserva) pelos ${DateUtils.remainingDaysInCurrentMonth()} dias restantes.",
                                            currentStatus = "Hoje você pode gastar até ${formatBrl(safeToSpend)} com tranquilidade sem comprometer o fechamento no azul.",
                                            oraclePrompt = "Meu limite seguro diário hoje é ${formatBrl(safeToSpend)}. Como posso otimizá-lo até o fim do mês?"
                                        )
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "${formatBrl(safeToSpend)} / dia",
                        style = StatCurrencyLarge,
                        color = safeColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${DateUtils.remainingDaysInCurrentMonth()} dias restantes no ciclo • Meta de reserva protegida",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )

                    Spacer(Modifier.height(14.dp))

                    // Caixa vs Cartão split (Equal-height 32-bit recessed bevel wells)
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
                            borderColor = LunarCyan.copy(alpha = 0.55f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.AccountBalanceWallet,
                                        tint = LunarCyan,
                                        size = 24.dp,
                                        iconSize = 14.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    TermLabelWithTooltip(
                                        termKey = TermKey.CHECKING_BALANCE,
                                        languageLevel = lang,
                                        textColor = OnSurfaceMuted,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = formatBrl(profile.checkingBalanceCents),
                                    style = StatCurrencyCompact,
                                    color = OnSurfaceText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        RpgInsetWell(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            borderColor = SolarMagenta.copy(alpha = 0.55f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.CreditCard,
                                        tint = SolarMagenta,
                                        size = 24.dp,
                                        iconSize = 14.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    TermLabelWithTooltip(
                                        termKey = TermKey.LOCKED_CARD,
                                        languageLevel = lang,
                                        textColor = OnSurfaceMuted,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Column {
                                    Text(
                                        text = formatBrl(profile.creditCardOpenBillCents),
                                        style = StatCurrencyCompact,
                                        color = SolarSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (profile.creditCardOpenBillCents > 0L) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Pagar Fatura (Neutro)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LunarCyan,
                                            modifier = Modifier
                                                .clickable(onClick = onPayOpenBill)
                                                .testTag("pay_open_bill_button")
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Break-Even Bar (Linha de Equilíbrio)
                    val be = state.breakEven
                    val beRatio = if (be.breakEvenCents > 0L) {
                        (be.monthlyIncomeCents.toFloat() / be.breakEvenCents.toFloat()).coerceIn(0f, 1.35f) / 1.35f
                    } else 1f
                    val markerAtBreakEven = 1f / 1.35f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TermLabelWithTooltip(
                            termKey = TermKey.BREAK_EVEN,
                            languageLevel = lang,
                            textColor = OnSurfaceText,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        ClickableHelpBadge(
                            contentDescription = "Entender Ponto de Equilíbrio",
                            testTag = "help_break_even",
                            onClick = {
                                onOpenExplainer(
                                    ExplainerContent(
                                        title = LanguageDictionary.get(TermKey.BREAK_EVEN, lang).label,
                                        whatItShows = "Soma seus custos fixos mensais, faturas/parcelas deste mês e média essencial variável. Passando dessa linha você entra na Zona de Conquista.",
                                        currentStatus = "Ponto de equilíbrio em ${formatBrl(be.breakEvenCents)} frente à receita de ${formatBrl(be.monthlyIncomeCents)}. ${if (be.isInConquestZone) "Você já venceu o equilíbrio com ${formatBrl(be.surplusOrDeficitCents)} livres!" else "Faltam ${formatBrl(-be.surplusOrDeficitCents)} para cobrir o básico."}",
                                        oraclePrompt = "Explique meu Ponto de Equilíbrio de ${formatBrl(be.breakEvenCents)} e como aumentar minha margem na Zona de Conquista."
                                    )
                                )
                            }
                        )
                    }

                    RpgProgressBar(
                        progress = beRatio,
                        dayMarkerFraction = markerAtBreakEven,
                        barColor = if (be.isInConquestZone) EmeraldSafe else AmberWarn,
                        secondaryColor = if (be.isInConquestZone) LunarCyan else CrimsonRisk,
                        height = 16.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Equilíbrio: ${formatBrl(be.breakEvenCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        StatusChip(
                            text = if (be.isInConquestZone) {
                                "${LanguageDictionary.get(TermKey.CONQUEST_ZONE, lang).label}: +${formatBrl(be.surplusOrDeficitCents)}"
                            } else {
                                "Faltam ${formatBrl(-be.surplusOrDeficitCents)}"
                            },
                            color = if (be.isInConquestZone) EmeraldSafe else AmberWarn
                        )
                    }
                }
            }

            // 4. Daily Educational Mission (3-step Narrative: Context -> Tension -> Action)
            item {
                val mission = state.stats.dailyMission
                val missionColor = when (mission.severity) {
                    MissionSeverity.SAFE -> EmeraldSafe
                    MissionSeverity.CAUTION -> AmberWarn
                    MissionSeverity.RISK -> CrimsonRisk
                }
                RpgBevelCard(borderColor = missionColor.copy(alpha = 0.65f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = missionColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Missão Educativa do Dia",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = missionColor
                            )
                        }
                        StatusChip(
                            text = when (mission.severity) {
                                MissionSeverity.SAFE -> "Ritmo Seguro"
                                MissionSeverity.CAUTION -> "Atenção"
                                MissionSeverity.RISK -> "Alerta"
                            },
                            color = missionColor
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(mission.contextText, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceText)
                    Spacer(Modifier.height(4.dp))
                    Text(mission.tensionText, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Ação: ${mission.actionText}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LunarPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            onAskOracleDirect("${mission.contextText} ${mission.actionText} O que você sugere?")
                        },
                        modifier = Modifier.align(Alignment.End).testTag("mission_ask_oracle")
                    ) {
                        Text("Simular plano no Oráculo ->", color = SolarSecondary)
                    }
                }
            }

            // 5. Category Budget Bars with Day-of-Month Marker
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TermLabelWithTooltip(
                        termKey = TermKey.FREE_BUDGET,
                        languageLevel = lang,
                        textColor = LunarPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(
                        text = "Dia ${DateUtils.currentDayOfMonth()}/${DateUtils.daysInCurrentMonth()}",
                        color = LunarCyan
                    )
                }
            }

            items(state.categoryStatuses, key = { it.category.id }) { status ->
                val usedPct = (status.ratio * 100).roundToInt()
                val statusColor = when {
                    status.ratio >= 0.95f -> CrimsonRisk
                    status.burnRateDeltaPct >= 15 || status.ratio >= 0.78f -> AmberWarn
                    else -> EmeraldSafe
                }

                RpgBevelCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RpgIconMedallion(
                            icon = categoryIcon(status.category.iconKey),
                            tint = statusColor,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = status.category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = OnSurfaceText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${status.category.percentageCap.roundToInt()}% • Teto ${formatBrl(status.capCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatBrl(status.spentCents),
                                style = StatCurrencySmall,
                                color = statusColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "$usedPct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        ClickableHelpBadge(
                            contentDescription = "Entender ${status.category.name}",
                            testTag = "help_cat_${status.category.id}",
                            onClick = {
                                val paceMsg = if (status.burnRateDeltaPct > 10) {
                                    "Ritmo ${status.burnRateDeltaPct}% acima do esperado para o dia de hoje."
                                } else {
                                    "Ritmo saudável dentro do esperado para a altura do mês."
                                }
                                onOpenExplainer(
                                    ExplainerContent(
                                        title = status.category.name,
                                        whatItShows = "A barra mostra quanto do teto mensal (${formatBrl(status.capCents)}) já foi consumido por competência. A linha branca marca o dia atual do mês.",
                                        currentStatus = "Gasto atual de ${formatBrl(status.spentCents)} ($usedPct%). $paceMsg",
                                        oraclePrompt = "Analise meu gasto em ${status.category.name} (${formatBrl(status.spentCents)} de ${formatBrl(status.capCents)})."
                                    )
                                )
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    RpgProgressBar(
                        progress = status.ratio,
                        dayMarkerFraction = if (status.category.fixedCost) null else dayProgress,
                        barColor = statusColor,
                        secondaryColor = LunarCyan,
                        height = 12.dp
                    )
                }
            }

            // 6. Recent Ledger Rows
            item {
                Text(
                    text = "Últimos Lançamentos",
                    style = MaterialTheme.typography.titleMedium,
                    color = LunarPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(state.transactions.take(8), key = { it.id }) { tx ->
                TransactionRowCard(tx = tx)
            }
        }

        if (showWisdomLoadoutDialog) {
            TavernaWisdomLoadoutDialog(
                profile = profile,
                onDismiss = { showWisdomLoadoutDialog = false }
            )
        }
    }
}

@Composable
private fun SoloGuildSwitch(
    viewMode: ViewMode,
    onSelect: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF070D1D))
            .border(1.dp, LunarCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val isSolo = viewMode == ViewMode.SOLO
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSolo) Brush.horizontalGradient(listOf(LunarCyan.copy(alpha = 0.28f), CosmicIndigo.copy(alpha = 0.7f)))
                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                )
                .then(
                    if (isSolo) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                    else Modifier
                )
                .clickable { onSelect(ViewMode.SOLO) }
                .testTag("switch_solo")
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NightsStay,
                    contentDescription = "Visão Solo",
                    tint = if (isSolo) LunarPrimary else OnSurfaceMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "Solo",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = if (isSolo) LunarPrimary else OnSurfaceMuted
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (!isSolo) Brush.horizontalGradient(listOf(SolarMagenta.copy(alpha = 0.35f), CosmicIndigo.copy(alpha = 0.75f)))
                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                )
                .then(
                    if (!isSolo) Modifier.border(1.dp, SolarMagenta.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                    else Modifier
                )
                .clickable { onSelect(ViewMode.GUILDA) }
                .testTag("switch_guilda")
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = "Visão Guilda",
                    tint = if (!isSolo) Color.White else OnSurfaceMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "Guilda",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = if (!isSolo) Color.White else OnSurfaceMuted
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .shadow(4.dp, CircleShape, spotColor = SolarMagenta)
                        .clip(CircleShape)
                        .background(SolarMagenta)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionRowCard(tx: TransactionRecord) {
    val type = safeEnumOf(tx.type, TransactionType.EXPENSE)
    val method = safeEnumOf(tx.paymentMethod, PaymentMethod.DEBIT_PIX_CASH)
    val isNeutral = AccountingEngine.isNeutralType(tx.type)
    val amountColor = when {
        isNeutral -> LunarPrimary
        type == TransactionType.INCOME -> EmeraldSafe
        else -> OnSurfaceText
    }
    val badgeText = when {
        type == TransactionType.TRANSFERENCIA_INTERNA -> "Transf. Neutra"
        type == TransactionType.PAGAMENTO_FATURA -> "Pagto Fatura"
        type == TransactionType.ACERTO_GUILDA -> "Acerto Guilda"
        method == PaymentMethod.CREDIT_CARD && tx.installmentTotal > 1 -> "Crédito ${tx.installmentIndex}/${tx.installmentTotal}"
        method == PaymentMethod.CREDIT_CARD -> "Cartão"
        else -> "Caixa/Pix"
    }

    RpgInsetWell(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isNeutral) LunarCyan.copy(alpha = 0.5f) else BevelBorderIndigo,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RpgIconMedallion(
                icon = if (isNeutral) Icons.Default.SwapHoriz
                else if (method == PaymentMethod.CREDIT_CARD) Icons.Default.CreditCard
                else Icons.Default.AccountBalanceWallet,
                tint = if (isNeutral) LunarCyan else if (method == PaymentMethod.CREDIT_CARD) SolarMagenta else EmeraldSafe,
                size = 36.dp,
                iconSize = 18.dp
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = OnSurfaceText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${DateUtils.epochDayToDisplay(tx.dateEpochDay)} • $badgeText",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                    if (tx.tags.isNotEmpty()) {
                        Text(
                            text = tx.tags.joinToString(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = LunarCyan
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = formatBrl(
                    cents = if (type == TransactionType.INCOME) tx.amountCents else -tx.amountCents,
                    includeSign = type == TransactionType.INCOME
                ),
                style = StatCurrencySmall,
                color = amountColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

fun categoryIcon(key: String): ImageVector = when (key.uppercase()) {
    "HOME" -> Icons.Default.Home
    "RESTAURANT" -> Icons.Default.Restaurant
    "HEALTH" -> Icons.Default.Favorite
    "SHIELD" -> Icons.Default.Shield
    else -> Icons.Default.Star
}
