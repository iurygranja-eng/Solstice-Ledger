package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSettings
import com.example.audio.RpgAudioEngine
import com.example.data.BudgetBucket
import com.example.data.DateUtils
import com.example.data.PaymentMethod
import com.example.data.TransactionType
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import com.example.domain.LanguageDictionary
import com.example.domain.StatsEngine
import com.example.domain.TermKey
import com.example.ui.components.ClickableHelpBadge
import com.example.ui.components.ExplainerContent
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.RpgIconMedallion
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgProgressBar
import com.example.ui.components.RpgPsxWindow
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
import com.example.ui.theme.LunarDeep
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.RoseHazard
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary
import com.example.ui.theme.StatCurrencyCompact
import com.example.ui.theme.StatCurrencyLarge
import com.example.ui.theme.StatCurrencyMedium
import com.example.ui.theme.StatCurrencySmall
import com.example.viewmodel.LedgerUiState
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private data class FutureMonthColumn(
    val label: String,
    val totalCommitCents: Long,
    val pctOfIncome: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapaEstrelasScreen(
    state: LedgerUiState.Ready,
    audioSettings: AudioSettings = AudioSettings(),
    onOpenExplainer: (ExplainerContent) -> Unit,
    onUnlockLesson: (String, Int) -> Unit,
    onAskOracle: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = state.profile.languageLevel
    val profile = state.profile
    val s = state.stats

    var selectedSection by remember { mutableIntStateOf(0) } // 0 = 5 Indicadores & Tags, 1 = Grimório
    var selectedCycle by remember { mutableIntStateOf(0) } // 0 = Ciclo Atual, 1 = Trimestre, 2 = Semestre
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    val dayOfMonth = DateUtils.currentDayOfMonth()
    val daysInMonth = DateUtils.daysInCurrentMonth().coerceAtLeast(1)
    val remainingDays = DateUtils.remainingDaysInCurrentMonth()
    val monthProgress = (dayOfMonth.toFloat() / daysInMonth.toFloat()).coerceIn(0.03f, 1f)
    val monthElapsedPct = (monthProgress * 100f).roundToInt()
    val currentMonth = DateUtils.currentYearMonth()

    // Real financial components for cards 2, 3, 4, 5
    val incomeCents = profile.monthlyNetIncomeCents.coerceAtLeast(100_00L)
    val monthExpenses = remember(state.transactions, currentMonth) {
        state.transactions.filter {
            it.competenceMonth == currentMonth &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }
    }
    val fixedCapCents = remember(state.budgets, incomeCents) {
        state.budgets.filter { it.fixedCost }
            .sumOf { ((incomeCents * it.percentageCap) / 100.0).roundToLong() }
    }
    val fixedPaidSoFar = remember(monthExpenses, state.budgets) {
        monthExpenses.filter { tx -> state.budgets.any { it.id == tx.categoryId && it.fixedCost } }
            .sumOf { it.amountCents }
    }
    val pendingFixed = (fixedCapCents - fixedPaidSoFar).coerceAtLeast(0L)
    val variableSpentSoFar = remember(monthExpenses, state.budgets) {
        monthExpenses.filter { tx -> state.budgets.none { it.id == tx.categoryId && it.fixedCost } }
            .sumOf { it.amountCents }
    }
    val projectedRemainingVar = if (dayOfMonth > 0) {
        ((variableSpentSoFar.toDouble() / dayOfMonth.toDouble()) * remainingDays).roundToLong()
    } else 0L

    val futureColumns = remember(state.transactions, currentMonth, incomeCents, fixedCapCents) {
        (1..3).map { offset ->
            val ym = DateUtils.addMonths(currentMonth, offset)
            val cardInstallments = state.transactions
                .filter {
                    it.competenceMonth == ym &&
                        safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE &&
                        safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD
                }
                .sumOf { it.amountCents }
            val totalCommit = fixedCapCents + cardInstallments
            val pct = ((totalCommit.toDouble() / incomeCents.toDouble()) * 100.0).roundToInt().coerceIn(0, 150)
            FutureMonthColumn(
                label = DateUtils.formatYearMonthShort(ym).substringBefore("/"),
                totalCommitCents = totalCommit,
                pctOfIncome = pct
            )
        }
    }

    val baselineSafeDailyCents = remember(incomeCents, profile.savingsGoalCents, fixedCapCents, daysInMonth) {
        ((incomeCents - profile.savingsGoalCents - fixedCapCents).coerceAtLeast(0L)) / daysInMonth.coerceAtLeast(1)
    }
    val safeDeltaPct = if (baselineSafeDailyCents > 0L) {
        (((s.safeToSpendDailyCents - baselineSafeDailyCents).toDouble() / baselineSafeDailyCents.toDouble()) * 100.0).roundToInt()
    } else 0

    val essentialMonthlyCost = remember(state.budgets, incomeCents) {
        state.budgets
            .filter { safeEnumOf(it.bucket, BudgetBucket.ESSENTIAL) == BudgetBucket.ESSENTIAL }
            .sumOf { ((incomeCents * it.percentageCap) / 100.0).roundToLong() }
            .coerceAtLeast(100_000L)
    }
    val sixMonthTargetCents = essentialMonthlyCost * 6L
    val runwayProgress = (profile.emergencyReserveCents.toFloat() / sixMonthTargetCents.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val runwayMissingCents = (sixMonthTargetCents - profile.emergencyReserveCents).coerceAtLeast(0L)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ================= HEADER DO MAPA DAS ESTRELAS (SISTEMA 2) =================
            item {
                RpgPsxWindow(
                    borderColor = LunarCyan.copy(alpha = 0.55f),
                    bracketColor = LunarCyan,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RpgIconMedallion(
                                icon = Icons.Default.AutoAwesome,
                                tint = LunarCyan,
                                size = 36.dp,
                                iconSize = 19.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SISTEMA 2 • PROJEÇÃO PREDITIVA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    ),
                                    color = LunarCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                TermLabelWithTooltip(
                                    termKey = TermKey.MAPA_TITLE,
                                    languageLevel = lang,
                                    textColor = LunarPrimary,
                                    style = CinzelTitleStyle.copy(fontSize = 16.sp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                RpgAudioEngine.playUiTap(audioSettings)
                                onOpenSettings()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("mapa_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = LunarCyan)
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Antecipa faturas invisíveis, ritmo de gasto e fôlego dos ciclos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusChip(
                            text = "Dia $dayOfMonth/$daysInMonth • $monthElapsedPct%",
                            color = LunarCyan
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Seletor de Seção: Projeções vs Grimório
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
                        listOf("5 Indicadores", "Grimório de Lições").forEachIndexed { idx, label ->
                            val active = selectedSection == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (active) Brush.horizontalGradient(listOf(LunarCyan.copy(alpha = 0.25f), CosmicIndigo))
                                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .then(
                                        if (active) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        selectedSection = idx
                                    }
                                    .testTag("mapa_tab_$idx")
                                    .padding(horizontal = 6.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) LunarPrimary else OnSurfaceMuted,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    if (selectedSection == 0) {
                        Spacer(Modifier.height(8.dp))
                        // Seletor de Ciclo / Constelação (1M | 3M | 6M)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MidnightBase)
                                .border(1.dp, LunarCyan.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Este Mês", "3 Meses", "6 Meses").forEachIndexed { idx, cycleLabel ->
                                val active = selectedCycle == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (active) CosmicIndigo.copy(alpha = 0.85f) else Color.Transparent)
                                        .then(
                                            if (active) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                                            else Modifier
                                        )
                                        .clickable {
                                            RpgAudioEngine.playUiTap(audioSettings)
                                            selectedCycle = idx
                                        }
                                        .testTag("mapa_cycle_$idx")
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cycleLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (active) LunarPrimary else OnSurfaceMuted,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        if (state.tagSummaries.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    Text(
                                        text = "FILTRO:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        color = LunarCyan
                                    )
                                }
                                item {
                                    val isAll = selectedTagFilter == null
                                    Surface(
                                        modifier = Modifier
                                            .clickable {
                                                RpgAudioEngine.playUiTap(audioSettings)
                                                selectedTagFilter = null
                                            }
                                            .testTag("mapa_tag_all"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isAll) CosmicIndigo else MidnightVoid,
                                        border = BorderStroke(1.dp, if (isAll) LunarCyan else BevelBorderIndigo)
                                    ) {
                                        Text(
                                            text = "Global",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isAll) LunarPrimary else OnSurfaceMuted,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                items(state.tagSummaries) { summary ->
                                    val active = selectedTagFilter == summary.tag
                                    Surface(
                                        modifier = Modifier
                                            .clickable {
                                                RpgAudioEngine.playUiTap(audioSettings)
                                                selectedTagFilter = if (active) null else summary.tag
                                            }
                                            .testTag("mapa_tag_${summary.tag}"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (active) SolarMagenta.copy(alpha = 0.22f) else MidnightVoid,
                                        border = BorderStroke(1.dp, if (active) SolarMagenta else BevelBorderIndigo)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(summary.tag, style = MaterialTheme.typography.labelSmall, color = SolarSecondary)
                                            Text(
                                                text = formatBrl(summary.totalCents),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = OnSurfaceMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Optional Tag Drilldown Banner when a tag filter is active
            if (selectedSection == 0 && selectedTagFilter != null) {
                item {
                    val activeTag = selectedTagFilter!!
                    val tagTxs = state.transactions.filter { tx ->
                        tx.tags.any { it.equals(activeTag, ignoreCase = true) || "#$it".equals(activeTag, ignoreCase = true) }
                    }
                    RpgBevelCard(borderColor = SolarMagenta.copy(alpha = 0.65f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = SolarMagenta, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Recorte Transversal: $activeTag", style = CinzelTitleStyle, color = SolarSecondary)
                            }
                            Text(
                                text = formatBrl(tagTxs.sumOf { it.amountCents }),
                                style = StatCurrencySmall,
                                color = LunarPrimary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        tagTxs.take(5).forEach { tx ->
                            Text(
                                text = "• ${tx.description} (${DateUtils.formatYearMonthShort(tx.competenceMonth)}) — ${formatBrl(tx.amountCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceText
                            )
                        }
                    }
                }
            }

            when (selectedSection) {
                0 -> {
                    // ================= 1. RITMO DE GASTO (BURN RATE) =================
                    item {
                        val hot = s.highestBurnCategory
                        val hotRatio = hot?.ratio ?: monthProgress
                        val hotPct = (hotRatio * 100f).roundToInt()
                        val isElevated = (hot?.burnRateDeltaPct ?: 0) >= 15
                        val burnAccent = when {
                            hotPct >= 95 -> CrimsonRisk
                            isElevated -> AmberWarn
                            else -> EmeraldSafe
                        }
                        val titleLabel = LanguageDictionary.get(TermKey.BURN_RATE, lang).label

                        RpgBevelCard(borderColor = burnAccent.copy(alpha = 0.60f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.LocalFireDepartment,
                                        tint = burnAccent,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "1. ${titleLabel.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = burnAccent
                                        )
                                        Text(
                                            text = if (hot != null && isElevated) "${hot.category.name} sob Tensão"
                                            else if (hot != null) "${hot.category.name} em Cadência Ideal"
                                            else "Ritmo Mensal Equilibrado",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurfaceText
                                        )
                                    }
                                }
                                ClickableHelpBadge(
                                    contentDescription = "Entender $titleLabel",
                                    testTag = "help_ind_burn",
                                    onClick = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onOpenExplainer(
                                            ExplainerContent(
                                                title = titleLabel,
                                                whatItShows = "Compara o percentual gasto da categoria contra o percentual de dias decorridos no mês ($monthElapsedPct%).",
                                                currentStatus = if (hot != null) "${hot.category.name} consumiu $hotPct% do teto (${formatBrl(hot.spentCents)} de ${formatBrl(hot.capCents)})." else "Sem desvios críticos.",
                                                oraclePrompt = "Como desacelerar meu ritmo de gastos em ${hot?.category?.name ?: "categorias variáveis"}?"
                                            )
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = burnAccent.copy(alpha = 0.4f)) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("CONSUMO REAL", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                        Text(
                                            text = if (hot != null) "$hotPct% Gasto (${formatBrl(hot.spentCents)})" else "Sem estouros",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = burnAccent
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    RpgProgressBar(
                                        progress = hotRatio,
                                        dayMarkerFraction = monthProgress,
                                        barColor = burnAccent,
                                        secondaryColor = if (isElevated) RoseHazard else LunarCyan,
                                        height = 14.dp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("▲ Marcador: Dia $dayOfMonth ($monthElapsedPct%)", style = MaterialTheme.typography.labelSmall, color = LunarCyan)
                                        Text("$hotPct% Consumido", style = MaterialTheme.typography.labelSmall, color = burnAccent)
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            val dailySaveSuggestion = if (hot != null) {
                                ((hot.spentCents / dayOfMonth.coerceAtLeast(1)) * 0.25).roundToLong().coerceAtLeast(1_500L)
                            } else 0L

                            NarrativeCalloutBox(
                                label = "TENSÃO:",
                                text = if (hot?.estimatedExhaustionDay != null) {
                                    "Previsão de término do teto de ${hot.category.name}: Dia ${hot.estimatedExhaustionDay} do mês."
                                } else {
                                    s.dailyMission.tensionText
                                },
                                accent = if (isElevated) AmberWarn else EmeraldSafe
                            )
                            Spacer(Modifier.height(6.dp))
                            NarrativeCalloutBox(
                                label = "AÇÃO:",
                                text = if (isElevated && hot != null) {
                                    "Segure cerca de ${formatBrl(dailySaveSuggestion)}/dia em ${hot.category.name} pelos próximos 5 dias para realinhar o ciclo."
                                } else {
                                    s.dailyMission.actionText
                                },
                                accent = LunarCyan
                            )
                        }
                    }

                    // ================= 2. SALDO PROJETADO DE FECHAMENTO =================
                    item {
                        val projColor = if (s.projectedClosingBalanceCents >= 0L) EmeraldSafe else CrimsonRisk
                        val titleLabel = LanguageDictionary.get(TermKey.PROJECTED_CLOSE, lang).label

                        RpgBevelCard(borderColor = projColor.copy(alpha = 0.60f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.Balance,
                                        tint = projColor,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "2. ${titleLabel.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = projColor
                                        )
                                        Text(
                                            text = "Previsão no Dia $daysInMonth",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurfaceText
                                        )
                                    }
                                }
                                ClickableHelpBadge(
                                    contentDescription = "Entender $titleLabel",
                                    testTag = "help_ind_close",
                                    onClick = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onOpenExplainer(
                                            ExplainerContent(
                                                title = titleLabel,
                                                whatItShows = "Simula quanto restará em conta corrente no último dia do mês se o ritmo atual continuar.",
                                                currentStatus = "Fechamento estimado em ${formatBrl(s.projectedClosingBalanceCents, includeSign = true)}.",
                                                oraclePrompt = "Como melhorar meu saldo projetado de fechamento (${formatBrl(s.projectedClosingBalanceCents)})?"
                                            )
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = projColor.copy(alpha = 0.45f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formatBrl(s.projectedClosingBalanceCents, includeSign = true),
                                            style = StatCurrencyLarge,
                                            color = projColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = if (s.projectedClosingBalanceCents >= 0L) "Zona Segura • Sem resgate forçado do baú"
                                            else "Risco de saldo negativo — revise variáveis",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    StatusChip(
                                        text = if (s.projectedClosingBalanceCents >= 0L) "Positivo" else "Deficitário",
                                        color = projColor
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = BevelBorderIndigo) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Fórmula Transparente", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LunarPrimary)
                                        Text(
                                            text = formatBrl(s.projectedClosingBalanceCents, includeSign = true),
                                            style = StatCurrencySmall,
                                            color = projColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Caixa Hoje", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                            Text(formatBrl(profile.checkingBalanceCents, includeSign = true), style = StatCurrencyCompact, color = LunarCyan, maxLines = 1, softWrap = false)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Fixos Pendentes", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                            Text("-${formatBrl(pendingFixed)}", style = StatCurrencyCompact, color = SolarSecondary, maxLines = 1, softWrap = false)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Fatura Aberta", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                            Text("-${formatBrl(profile.creditCardOpenBillCents)}", style = StatCurrencyCompact, color = SolarSecondary, maxLines = 1, softWrap = false)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Variáveis Restantes", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                            Text("-${formatBrl(projectedRemainingVar)}", style = StatCurrencyCompact, color = OnSurfaceText, maxLines = 1, softWrap = false)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= 3. COMPROMETIMENTO FUTURO (RENDA PRÉ-OCUPADA) =================
                    item {
                        val activePct = when (selectedCycle) {
                            1 -> s.threeMonthCommitmentPct
                            2 -> s.sixMonthCommitmentPct
                            else -> s.nextMonthCommitmentPct
                        }
                        val cycleTag = when (selectedCycle) {
                            1 -> "média em 3 meses"
                            2 -> "média em 6 meses"
                            else -> "da renda no próximo ciclo"
                        }
                        val commColor = when {
                            activePct > 65 -> CrimsonRisk
                            activePct > 50 -> AmberWarn
                            else -> EmeraldSafe
                        }
                        val statusBadge = when {
                            activePct > 65 -> "Crítico (>65%)"
                            activePct > 50 -> "Atenção (50-65%)"
                            else -> "Confortável (<50%)"
                        }
                        val titleLabel = LanguageDictionary.get(TermKey.FUTURE_COMMITMENT, lang).label

                        RpgBevelCard(borderColor = commColor.copy(alpha = 0.60f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.HourglassTop,
                                        tint = LunarCyan,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "3. ${titleLabel.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LunarCyan
                                        )
                                        Text(
                                            text = "Renda Pré-Ocupada (Fixos + Cartão)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurfaceText
                                        )
                                    }
                                }
                                ClickableHelpBadge(
                                    contentDescription = "Entender $titleLabel",
                                    testTag = "help_ind_commit",
                                    onClick = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onOpenExplainer(
                                            ExplainerContent(
                                                title = titleLabel,
                                                whatItShows = "Mostra quanto da renda dos próximos meses já nasce travada por custos fixos e parcelas futuras no cartão.",
                                                currentStatus = "Próx. mês: ${s.nextMonthCommitmentPct}% • 3m: ${s.threeMonthCommitmentPct}% • 6m: ${s.sixMonthCommitmentPct}%.",
                                                oraclePrompt = "Meu comprometimento futuro está em ${s.nextMonthCommitmentPct}%. Devo evitar novos parcelamentos?"
                                            )
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$activePct% $cycleTag",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                    color = OnSurfaceText,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                StatusChip(text = statusBadge, color = commColor)
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.35f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    futureColumns.forEach { col ->
                                        val barFraction = (col.pctOfIncome.toFloat() / 100f).coerceIn(0.08f, 1f)
                                        val colAccent = when {
                                            col.pctOfIncome > 65 -> CrimsonRisk
                                            col.pctOfIncome > 50 -> AmberWarn
                                            else -> LunarCyan
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = col.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = OnSurfaceText,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(84.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MidnightBase)
                                                    .border(1.dp, BevelBorderIndigo, RoundedCornerShape(6.dp))
                                                    .padding(3.dp),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(barFraction)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(SolarMagenta.copy(alpha = 0.85f), colAccent, LunarDeep)
                                                            )
                                                        )
                                                )
                                                Text(
                                                    text = "${col.pctOfIncome}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = JetBrainsMonoFontFamily
                                                    ),
                                                    color = Color.White,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                            Text(
                                                text = formatBrl(col.totalCommitCents),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = colAccent,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            val reliefCents = (futureColumns.first().totalCommitCents - futureColumns.last().totalCommitCents).coerceAtLeast(0L)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (reliefCents > 0L) {
                                    "Suas parcelas reduzem gradualmente, liberando ${formatBrl(reliefCents)} extras até ${futureColumns.last().label}."
                                } else {
                                    "Comprometimento estável nos próximos 3 ciclos (${futureColumns.first().pctOfIncome}%)."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    // ================= 4. SAFE-TO-SPEND ADAPTATIVO =================
                    item {
                        val titleLabel = LanguageDictionary.get(TermKey.SAFE_TO_SPEND, lang).label
                        RpgPsxWindow(
                            borderColor = LunarCyan.copy(alpha = 0.65f),
                            bracketColor = LunarCyan,
                            contentPadding = PaddingValues(15.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.Shield,
                                        tint = LunarCyan,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "4. ${titleLabel.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LunarCyan
                                        )
                                        Text(
                                            text = "Limite Diário Adaptativo",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LunarPrimary
                                        )
                                    }
                                }
                                ClickableHelpBadge(
                                    contentDescription = "Entender $titleLabel",
                                    testTag = "help_ind_safe",
                                    onClick = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onOpenExplainer(
                                            ExplainerContent(
                                                title = titleLabel,
                                                whatItShows = "Calcula quanto você pode gastar por dia nos dias restantes do mês protegendo fixos, faturas e reserva.",
                                                currentStatus = "Hoje: ${formatBrl(s.safeToSpendDailyCents)}/dia para os $remainingDays dias restantes.",
                                                oraclePrompt = "Como otimizar meu limite seguro diário de ${formatBrl(s.safeToSpendDailyCents)}?"
                                            )
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.45f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${formatBrl(s.safeToSpendDailyCents)} / dia",
                                            style = StatCurrencyLarge,
                                            color = LunarPrimary
                                        )
                                        Text(
                                            text = "Para os $remainingDays dias restantes de ${DateUtils.formatYearMonthShort(currentMonth)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceMuted
                                        )
                                    }
                                    if (baselineSafeDailyCents > 0L && baselineSafeDailyCents != s.safeToSpendDailyCents) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = formatBrl(baselineSafeDailyCents),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    textDecoration = TextDecoration.LineThrough
                                                ),
                                                color = OnSurfaceMuted
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            StatusChip(
                                                text = if (safeDeltaPct < 0) "$safeDeltaPct% Ajustado" else "+$safeDeltaPct% Folga",
                                                color = if (safeDeltaPct < 0) SolarMagenta else EmeraldSafe
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Recalculado automaticamente após seus lançamentos reais, protegendo sua meta de reserva mensal (${formatBrl(profile.savingsGoalCents)}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceText
                            )
                        }
                    }

                    // ================= 5. HORIZONTE DO ESCUDO (RUNWAY) =================
                    item {
                        val titleLabel = LanguageDictionary.get(TermKey.RUNWAY, lang).label
                        val runwayColor = if (s.runwayMonths >= 6.0) EmeraldSafe else LunarCyan
                        val runwayPct = (runwayProgress * 100f).roundToInt()

                        RpgBevelCard(borderColor = SolarMagenta.copy(alpha = 0.55f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RpgIconMedallion(
                                        icon = Icons.Default.Diamond,
                                        tint = SolarMagenta,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "5. ${titleLabel.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SolarSecondary
                                        )
                                        Text(
                                            text = "Reserva de Sobrevivência",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurfaceText
                                        )
                                    }
                                }
                                ClickableHelpBadge(
                                    contentDescription = "Entender $titleLabel",
                                    testTag = "help_ind_runway",
                                    onClick = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        onOpenExplainer(
                                            ExplainerContent(
                                                title = titleLabel,
                                                whatItShows = "Divide sua reserva líquida pelo custo mensal essencial, indicando quantos meses de autonomia você possui.",
                                                currentStatus = "Reserva de ${formatBrl(profile.emergencyReserveCents)} = ${String.format(Locale.US, "%.1f", s.runwayMonths)} meses. Meta 6m em ${s.projectedTargetMonthLabel}.",
                                                oraclePrompt = "Estou com ${String.format(Locale.US, "%.1f", s.runwayMonths)} meses de reserva. Como atingir 6 meses mais rápido?"
                                            )
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            RpgInsetWell(borderColor = runwayColor.copy(alpha = 0.45f)) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", s.runwayMonths)} Meses",
                                                style = StatCurrencyMedium,
                                                color = OnSurfaceText,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = "${formatBrl(profile.emergencyReserveCents)} • Essencial ${formatBrl(essentialMonthlyCost)}/m",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceMuted
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Meta: 6.0m",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = LunarCyan,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = formatBrl(sixMonthTargetCents),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = OnSurfaceMuted,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    RpgProgressBar(
                                        progress = runwayProgress,
                                        barColor = LunarCyan,
                                        secondaryColor = SolarMagenta,
                                        height = 14.dp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$runwayPct% Concluído", style = MaterialTheme.typography.labelSmall, color = EmeraldSafe)
                                        Text(
                                            text = if (runwayMissingCents > 0L) "Faltam ${formatBrl(runwayMissingCents)}" else "Fortaleza Completa!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LunarCyan
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (runwayMissingCents > 0L) {
                                    "Mantendo aportes de ${formatBrl(profile.savingsGoalCents)}/mês, o 6º mês de proteção será alcançado em ${s.projectedTargetMonthLabel ?: "-"}."
                                } else {
                                    "Escudo máximo forjado! Você já possui 6+ meses de autonomia financeira garantida."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    // ================= CALENDÁRIO ASTRAL DE PARCELAS =================
                    item {
                        val futureMonths = remember(state.futureInstallments, currentMonth) {
                            state.futureInstallments.filter { it.yearMonth != currentMonth }
                        }
                        val nextMonthTotal = futureMonths.firstOrNull()?.totalCents ?: 0L

                        RpgBevelCard(borderColor = SolarMagenta.copy(alpha = 0.55f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = SolarMagenta, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Calendário de Parcelas",
                                        style = CinzelTitleStyle,
                                        color = SolarSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                StatusChip(text = "Próximos Ciclos", color = LunarCyan)
                            }
                            Spacer(Modifier.height(10.dp))
                            if (state.futureInstallments.isEmpty()) {
                                Text("Nenhuma compra parcelada ou fatura ativa nos próximos ciclos.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            } else {
                                RpgInsetWell(borderColor = SolarMagenta.copy(alpha = 0.35f), contentPadding = PaddingValues(10.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        state.futureInstallments.forEachIndexed { idx, monthSummary ->
                                            if (idx > 0) {
                                                HorizontalDivider(color = BevelBorderIndigo.copy(alpha = 0.6f))
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(monthSummary.displayLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LunarPrimary)
                                                Text(formatBrl(monthSummary.totalCents), style = StatCurrencySmall, color = SolarSecondary, maxLines = 1, softWrap = false)
                                            }
                                            monthSummary.items.forEach { tx ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "• ${tx.description}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = OnSurfaceText,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "-${formatBrl(tx.amountCents)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = RoseHazard,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                        if (nextMonthTotal > 0L) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MidnightBase)
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("TOTAL BLOQUEADO PRÓX. MÊS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                                    Text("-${formatBrl(nextMonthTotal)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SolarSecondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= GRIMÓRIO DE SABEDORIA (KAHNEMAN) =================
                    item {
                        RpgBevelCard(borderColor = LunarCyan.copy(alpha = 0.45f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "GRIMÓRIO ASTRAL • SABEDORIA COMPORTAMENTAL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LunarCyan
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "O Paradoxo da Disponibilidade (Daniel Kahneman)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceText
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "O Sistema 1 (impulsivo) enxerga apenas o saldo em caixa hoje (${formatBrl(profile.checkingBalanceCents)}). O Mapa das Estrelas (Sistema 2) obriga a mente a descontar a fatura aberta (${formatBrl(profile.creditCardOpenBillCents)}) e parcelas futuras antes de decidir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    // ================= EPIC CTA: CONSULTAR ORÁCULO =================
                    item {
                        val hotCatText = s.highestBurnCategory?.let {
                            "${it.category.name} em ${(it.ratio * 100).roundToInt()}%"
                        } ?: "categorias equilibradas"
                        LunarCoreButton(
                            text = "Consultar Oráculo sobre os 5 Indicadores",
                            onClick = {
                                RpgAudioEngine.playUiTap(audioSettings)
                                onAskOracle(
                                    "Analise meus 5 indicadores do Mapa das Estrelas: Burn Rate ($hotCatText), Fechamento Projetado (${formatBrl(s.projectedClosingBalanceCents)}), Comprometimento Futuro (${s.nextMonthCommitmentPct}%), Limite Seguro Diário (${formatBrl(s.safeToSpendDailyCents)}/dia) e Runway (${String.format(Locale.US, "%.1f", s.runwayMonths)} meses). Qual é minha prioridade de ação hoje?"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "mapa_ask_oracle_button"
                        )
                    }
                }

                else -> {
                    // GRIMÓRIO: Unlockable bite-sized financial lessons
                    items(StatsEngine.allLessons, key = { it.id }) { lesson ->
                        val unlocked = lesson.id in profile.unlockedLessons
                        RpgBevelCard(
                            borderColor = if (unlocked) EmeraldSafe.copy(alpha = 0.6f) else BevelBorderIndigo
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Default.AutoStories else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (unlocked) EmeraldSafe else AmberWarn,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = lesson.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (unlocked) LunarPrimary else OnSurfaceText
                                    )
                                }
                                StatusChip(
                                    text = if (unlocked) "Lido" else "+${lesson.xpReward} XP",
                                    color = if (unlocked) EmeraldSafe else AmberWarn
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (unlocked) {
                                Text(lesson.summary, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceText)
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSafe, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(lesson.keyTakeaway, style = MaterialTheme.typography.bodySmall, color = EmeraldSafe)
                                }
                            } else {
                                Text(
                                    text = "Toque abaixo para estudar este pergaminho e ganhar +${lesson.xpReward} XP de Sabedoria.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceMuted
                                )
                                Spacer(Modifier.height(10.dp))
                                LunarCoreButton(
                                    text = "Desbloquear Lição",
                                    onClick = {
                                        RpgAudioEngine.playSuccessChime(audioSettings)
                                        onUnlockLesson(lesson.id, lesson.xpReward)
                                    },
                                    testTag = "unlock_${lesson.id}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NarrativeCalloutBox(
    label: String,
    text: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(MidnightBase.copy(alpha = 0.90f))
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily
            ),
            color = accent
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceText,
            modifier = Modifier.weight(1f)
        )
    }
}
