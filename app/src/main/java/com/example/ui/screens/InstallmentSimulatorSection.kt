package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.PaymentMethod
import com.example.data.TransactionType
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.RpgDangerWindow
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgPsxWindow
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
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
import com.example.ui.theme.PsxWindowBottom
import com.example.ui.theme.RoseHazard
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary
import com.example.ui.theme.StatCurrencySmall
import com.example.viewmodel.LedgerUiState
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstallmentSimulatorSection(
    state: LedgerUiState.Ready,
    isSubmitting: Boolean,
    onConfirmPurchase: (
        description: String,
        amountCents: Long,
        paymentMethod: PaymentMethod,
        categoryId: String,
        tags: List<String>,
        installments: Int
    ) -> Unit,
    onAskOracle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val profile = state.profile
    val currentMonth = DateUtils.currentYearMonth()

    var simDescription by remember { mutableStateOf("Setup Workstation / Compra Planejada") }
    var simReaisText by remember { mutableStateOf("3600") }
    var simPaymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }
    var simInstallments by remember { mutableIntStateOf(12) }
    var simCategoryId by remember {
        mutableStateOf(
            state.budgets.firstOrNull { !it.fixedCost }?.id
                ?: state.budgets.firstOrNull()?.id
                ?: "cat_lazer"
        )
    }
    var simTag by remember { mutableStateOf("#Trabalho") }
    var downPaymentHalfApplied by remember { mutableStateOf(false) }

    val rawTotalCents = ((simReaisText.toLongOrNull() ?: 0L) * 100L).coerceIn(0L, 100_000_000_00L)
    val effectiveInstallments = if (simPaymentMethod == PaymentMethod.CREDIT_CARD) simInstallments else 1
    val simulatedCents = if (downPaymentHalfApplied) (rawTotalCents / 2L) else rawTotalCents
    val monthlyInstallmentCents = if (effectiveInstallments > 0) simulatedCents / effectiveInstallments else simulatedCents

    // Compute real 6-month baseline from user's budgets + transactions
    val incomeCents = profile.monthlyNetIncomeCents.coerceAtLeast(100_00L)
    val fixedBaselineCents = remember(state.budgets, incomeCents) {
        state.budgets
            .filter { it.fixedCost }
            .sumOf { ((incomeCents * it.percentageCap) / 100.0).roundToLong() }
    }

    val sixMonthKeys = remember(currentMonth) { (0..5).map { DateUtils.addMonths(currentMonth, it) } }
    val baselineBillsByMonth = remember(state.transactions, sixMonthKeys, profile.creditCardOpenBillCents) {
        sixMonthKeys.mapIndexed { idx, ym ->
            val cardTxs = state.transactions.filter {
                it.competenceMonth == ym &&
                    safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE &&
                    safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD
            }.sumOf { it.amountCents }
            if (idx == 0) maxOf(profile.creditCardOpenBillCents, cardTxs) else cardTxs
        }
    }

    val baselineCommitPct = (((fixedBaselineCents + baselineBillsByMonth.getOrElse(0) { 0L }).toDouble() / incomeCents.toDouble()) * 100.0)
        .roundToInt().coerceIn(0, 150)

    val addedMonthlyCommitment = if (simPaymentMethod == PaymentMethod.CREDIT_CARD) monthlyInstallmentCents else simulatedCents
    val projectedBillsByMonth = baselineBillsByMonth.mapIndexed { idx, base ->
        val extra = if (idx < effectiveInstallments) monthlyInstallmentCents else 0L
        base + extra
    }

    val projectedCommitPct = (((fixedBaselineCents + projectedBillsByMonth.getOrElse(0) { 0L }).toDouble() / incomeCents.toDouble()) * 100.0)
        .roundToInt().coerceIn(0, 180)

    val currentSafeDaily = state.stats.safeToSpendDailyCents
    val remainingDays = DateUtils.remainingDaysInCurrentMonth().coerceAtLeast(1)
    val projectedSafeDaily = (currentSafeDaily - (monthlyInstallmentCents / remainingDays)).coerceAtLeast(0L)
    val safeDropPerDay = (currentSafeDaily - projectedSafeDaily).coerceAtLeast(0L)

    val prudentCapCents = (incomeCents * 0.50).roundToLong() - fixedBaselineCents
    val prudentBillCap = prudentCapCents.coerceAtLeast(incomeCents / 5L)

    val isCritical = projectedCommitPct > 50 || projectedBillsByMonth.any { it > prudentBillCap }
    val isCaution = !isCritical && projectedCommitPct >= 38
    val alertAccent = when {
        isCritical -> RoseHazard
        isCaution -> AmberWarn
        else -> EmeraldSafe
    }

    val endMonthLabel = DateUtils.formatYearMonthShort(DateUtils.addMonths(currentMonth, (effectiveInstallments - 1).coerceAtLeast(0)))
    val startMonthLabel = DateUtils.formatYearMonthShort(currentMonth)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 32-Bit PSX Main Window
        RpgPsxWindow(
            borderColor = LunarCyan.copy(alpha = 0.45f),
            bracketColor = LunarCyan,
            contentPadding = PaddingValues(14.dp)
        ) {
            // Window Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.linearGradient(listOf(CosmicIndigo, MidnightBase)))
                            .border(1.dp, LunarCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = LunarPrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Simular Compra no Cartão", style = CinzelTitleStyle, color = LunarPrimary)
                        Text(
                            text = "Análise preditiva com seus dados reais",
                            style = MaterialTheme.typography.labelSmall,
                            color = LunarCyan.copy(alpha = 0.78f)
                        )
                    }
                }
                Text(
                    text = "◆ 32-BIT ◆",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LunarCyan
                )
            }

            // Form Inset Well
            RpgInsetWell(
                borderColor = LunarCyan.copy(alpha = 0.35f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Description + Tag
                    Text("ITEM / DESCRIÇÃO DO ESPÓLIO", style = MaterialTheme.typography.labelSmall, color = LunarPrimary)
                    OutlinedTextField(
                        value = simDescription,
                        onValueChange = { if (it.length <= 80) simDescription = it },
                        singleLine = true,
                        trailingIcon = {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CosmicIndigo.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, SolarMagenta.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = simTag,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SolarSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LunarCyan,
                            unfocusedBorderColor = BevelBorderIndigo,
                            focusedContainerColor = MidnightBase,
                            unfocusedContainerColor = MidnightBase,
                            focusedTextColor = OnSurfaceText,
                            unfocusedTextColor = OnSurfaceText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("sim_description_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("#Trabalho", "#Casa", "#ViagemFérias", "#Reforma", "#Pet").forEach { tag ->
                            val active = simTag == tag
                            Surface(
                                modifier = Modifier.clickable { simTag = tag },
                                shape = RoundedCornerShape(4.dp),
                                color = if (active) SolarMagenta.copy(alpha = 0.22f) else MidnightVoid,
                                border = BorderStroke(1.dp, if (active) SolarMagenta else BevelBorderIndigo)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) SolarSecondary else OnSurfaceMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Category (Mana Vessel)
                    Text("RECIPIENTE DE MANA (CATEGORIA)", style = MaterialTheme.typography.labelSmall, color = LunarPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.budgets.forEach { cat: BudgetCategory ->
                            val selected = simCategoryId == cat.id
                            Surface(
                                modifier = Modifier.clickable { simCategoryId = cat.id },
                                shape = RoundedCornerShape(6.dp),
                                color = if (selected) CosmicIndigo else MidnightVoid,
                                border = BorderStroke(1.2.dp, if (selected) LunarCyan else BevelBorderIndigo)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(cat.iconKey),
                                        contentDescription = null,
                                        tint = if (selected) LunarCyan else OnSurfaceMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) LunarPrimary else OnSurfaceMuted
                                    )
                                }
                            }
                        }
                    }

                    // Price & Payment Method
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(0.95f)) {
                            Text("PREÇO (R$)", style = MaterialTheme.typography.labelSmall, color = LunarPrimary)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = simReaisText,
                                onValueChange = { txt ->
                                    val digits = txt.filter { it.isDigit() }
                                    if (digits.length <= 7) {
                                        simReaisText = digits
                                        downPaymentHalfApplied = false
                                    }
                                },
                                prefix = { Text("R$ ", fontFamily = JetBrainsMonoFontFamily, color = LunarCyan) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = StatCurrencySmall.copy(color = GoldBadge),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldBadge,
                                    unfocusedBorderColor = BevelBorderIndigo,
                                    focusedContainerColor = MidnightBase,
                                    unfocusedContainerColor = MidnightBase
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("sim_amount_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1.05f)) {
                            Text("GRIMÓRIO DE PAGTO", style = MaterialTheme.typography.labelSmall, color = LunarPrimary)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MidnightBase)
                                    .border(1.dp, BevelBorderIndigo, RoundedCornerShape(6.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                val isCard = simPaymentMethod == PaymentMethod.CREDIT_CARD
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isCard) CosmicIndigo else Color.Transparent)
                                        .border(1.dp, if (isCard) SolarMagenta else Color.Transparent, RoundedCornerShape(4.dp))
                                        .clickable { simPaymentMethod = PaymentMethod.CREDIT_CARD }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = if (isCard) SolarSecondary else OnSurfaceMuted, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Cartão", style = MaterialTheme.typography.labelSmall, color = if (isCard) SolarSecondary else OnSurfaceMuted)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (!isCard) CosmicIndigo else Color.Transparent)
                                        .border(1.dp, if (!isCard) EmeraldSafe else Color.Transparent, RoundedCornerShape(4.dp))
                                        .clickable { simPaymentMethod = PaymentMethod.DEBIT_PIX_CASH }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = if (!isCard) EmeraldSafe else OnSurfaceMuted, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Pix", style = MaterialTheme.typography.labelSmall, color = if (!isCard) EmeraldSafe else OnSurfaceMuted)
                                    }
                                }
                            }
                        }
                    }

                    // Installments Row
                    if (simPaymentMethod == PaymentMethod.CREDIT_CARD) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DIVISÃO DE FATURAS", style = MaterialTheme.typography.labelSmall, color = LunarPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = alertAccent, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${effectiveInstallments}x de ${formatBrl(monthlyInstallmentCents)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = alertAccent
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 3, 6, 10, 12).forEach { count ->
                                val active = simInstallments == count
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (active) Brush.verticalGradient(listOf(RoseHazard, Color(0xFF9F1239)))
                                            else Brush.verticalGradient(listOf(MidnightVoid, MidnightBase))
                                        )
                                        .border(
                                            1.2.dp,
                                            if (active) Color(0xFFFDA4AF) else BevelBorderIndigo,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { simInstallments = count }
                                        .testTag("sim_inst_$count")
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${count}x",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (active) Color.White else OnSurfaceMuted
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Início: $startMonthLabel", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                            Text("Término: $endMonthLabel ($effectiveInstallments Luas)", style = MaterialTheme.typography.labelSmall, color = alertAccent)
                        }
                    }

                    if (downPaymentHalfApplied) {
                        Text(
                            text = "Simulação recalculada com 50% de entrada à vista — valor parcelado reduzido para ${formatBrl(simulatedCents)}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSafe
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 4. Painel de Ressonância Temporal (Danger / Caution / Safe Window)
            RpgDangerWindow(
                accentColor = alertAccent,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isCritical || isCaution) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = null,
                            tint = alertAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Ressonância Temporal",
                            style = CinzelTitleStyle.copy(fontSize = 13.sp),
                            color = alertAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = alertAccent.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, alertAccent)
                    ) {
                        Text(
                            text = when {
                                isCritical -> "RISCO CRÍTICO (>50%)"
                                isCaution -> "ATENÇÃO (38-50%)"
                                else -> "SEGURO (<38%)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                            color = OnSurfaceText,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 2x2 Comparative Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimMetricTile(
                        title = "Comprometimento",
                        before = "$baselineCommitPct%",
                        after = "$projectedCommitPct%",
                        caption = if (projectedCommitPct > 50) "Teto de 50% rompido" else "Dentro da zona prudente",
                        accent = alertAccent,
                        isUp = true,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    SimMetricTile(
                        title = "Fatura $startMonthLabel",
                        before = formatBrl(baselineBillsByMonth.getOrElse(0) { 0L }),
                        after = formatBrl(projectedBillsByMonth.getOrElse(0) { 0L }),
                        caption = "+${formatBrl(monthlyInstallmentCents)} neste ciclo",
                        accent = alertAccent,
                        isUp = null,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimMetricTile(
                        title = "Safe-to-Spend",
                        before = "${formatBrl(currentSafeDaily)}/d",
                        after = "${formatBrl(projectedSafeDaily)}/d",
                        caption = "-${formatBrl(safeDropPerDay)}/dia de margem",
                        accent = alertAccent,
                        isUp = false,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    SimMetricTile(
                        title = "Duração do Feitiço",
                        before = "",
                        after = if (effectiveInstallments > 1) "$effectiveInstallments Luas" else "À Vista",
                        caption = if (effectiveInstallments > 1) "Compromisso contínuo" else "Sem parcelas futuras",
                        accent = alertAccent,
                        isUp = null,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 6-Month Horizon Chart (Canvas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MidnightBase.copy(alpha = 0.85f))
                        .border(1.dp, alertAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = alertAccent, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Horizonte de Faturas (6 Luas)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceText)
                            }
                            Text(
                                text = "Teto ${formatBrl(prudentBillCap)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = alertAccent
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizonBarChart(
                            monthLabels = sixMonthKeys.map { DateUtils.formatYearMonthShort(it).substringBefore("/") },
                            baselineCents = baselineBillsByMonth,
                            addedCents = sixMonthKeys.indices.map { idx -> if (idx < effectiveInstallments) monthlyInstallmentCents else 0L },
                            prudentCapCents = prudentBillCap,
                            hazardColor = alertAccent
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 3-Step Storytelling Oracle Verdict
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PsxWindowBottom.copy(alpha = 0.85f))
                        .border(1.dp, alertAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NarrativeStepRow(
                        step = "1",
                        label = "Contexto:",
                        body = "Seu comprometimento mensal vai de $baselineCommitPct% para $projectedCommitPct% da renda.",
                        accent = alertAccent
                    )
                    NarrativeStepRow(
                        step = "2",
                        label = "Tensão:",
                        body = "Compromete ${formatBrl(addedMonthlyCommitment)}/mês e reduz sua margem diária para ${formatBrl(projectedSafeDaily)}/dia.",
                        accent = alertAccent
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(alertAccent.copy(alpha = 0.14f))
                            .border(1.dp, alertAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(alertAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when {
                                    isCritical -> "ALERTA DO ORÁCULO: Compra Desaconselhada"
                                    isCaution -> "CONSELHO DO ORÁCULO: Avalie Entrada"
                                    else -> "ORÁCULO APROVA: Dentro da Zona Segura"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceText
                            )
                            Text(
                                text = when {
                                    isCritical -> "Recomendação: junte ${formatBrl(rawTotalCents / 2L)} para entrada de 50% ou consulte o Oráculo para ajustar tetos."
                                    isCaution -> "Parcelamento viável, mas reduz sua folga diária em ${formatBrl(safeDropPerDay)}/dia."
                                    else -> "Impacto absorvido com folga pelo seu orçamento atual."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = LunarPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Primary Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LunarCoreButton(
                    text = if (downPaymentHalfApplied) "Restaurar Valor Integral (100%)" else "Recalcular com Entrada de 50%",
                    enabled = rawTotalCents > 0L,
                    onClick = { downPaymentHalfApplied = !downPaymentHalfApplied },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sim_recalculate_half_button"
                )

                Button(
                    onClick = {
                        onAskOracle(
                            "Estou simulando '${simDescription.ifBlank { "Compra" }}' de ${formatBrl(simulatedCents)} em ${effectiveInstallments}x de ${formatBrl(monthlyInstallmentCents)}. Meu comprometimento iria para $projectedCommitPct% e meu Safe-to-Spend cairia para ${formatBrl(projectedSafeDaily)}/dia. Vale a pena ou como devo planejar?"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicIndigo,
                        contentColor = SolarSecondary
                    ),
                    border = BorderStroke(1.2.dp, SolarMagenta.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("sim_ask_oracle_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Consultar Alternativa com Oráculo (IA)", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        if (simulatedCents > 0L) {
                            onConfirmPurchase(
                                simDescription.ifBlank { "Compra Simulada" },
                                simulatedCents,
                                simPaymentMethod,
                                simCategoryId,
                                listOf(simTag),
                                effectiveInstallments
                            )
                        }
                    },
                    enabled = simulatedCents > 0L && !isSubmitting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCritical) Color(0xFF1F0A14) else EmeraldSafe.copy(alpha = 0.22f),
                        contentColor = if (isCritical) Color(0xFFFDA4AF) else EmeraldSafe
                    ),
                    border = BorderStroke(1.2.dp, if (isCritical) RoseHazard.copy(alpha = 0.7f) else EmeraldSafe),
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("sim_commit_button")
                ) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isSubmitting) "Registrando..." else "Registrar Compra (${effectiveInstallments}x ${formatBrl(monthlyInstallmentCents)})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Descartar Simulação / Voltar à Taverna", style = MaterialTheme.typography.labelSmall, color = LunarCyan)
                }
            }

            // Accrual Accounting Ledger Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Regime de Competência: a 1ª parcela entra na fatura de $startMonthLabel sem deduzir seu saldo em caixa hoje, agendando $effectiveInstallments obrigação(ões) futuras.",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimMetricTile(
    title: String,
    before: String,
    after: String,
    caption: String,
    accent: Color,
    isUp: Boolean?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MidnightBase.copy(alpha = 0.88f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = accent.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (before.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = before,
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDecoration = if (isUp == null) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = OnSurfaceMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                        Icon(
                            imageVector = when (isUp) {
                                true -> Icons.AutoMirrored.Filled.TrendingUp
                                false -> Icons.AutoMirrored.Filled.TrendingDown
                                null -> Icons.AutoMirrored.Filled.ArrowForward
                            },
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = after,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = OnSurfaceText,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = accent
        )
    }
}

@Composable
private fun NarrativeStepRow(
    step: String,
    label: String,
    body: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accent.copy(alpha = 0.25f))
                .border(1.dp, accent, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(step, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = OnSurfaceText)
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = "$label $body",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceText
        )
    }
}

@Composable
private fun HorizonBarChart(
    monthLabels: List<String>,
    baselineCents: List<Long>,
    addedCents: List<Long>,
    prudentCapCents: Long,
    hazardColor: Color
) {
    val maxVal = maxOf(
        prudentCapCents,
        baselineCents.indices.maxOfOrNull { baselineCents[it] + addedCents.getOrElse(it) { 0L } } ?: 1L
    ).coerceAtLeast(100_00L)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
        ) {
            val w = size.width
            val h = size.height
            val chartTop = 8f
            val chartHeight = (h - chartTop - 4f).coerceAtLeast(20f)

            val capY = chartTop + chartHeight * (1f - (prudentCapCents.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
            drawLine(
                color = hazardColor.copy(alpha = 0.8f),
                start = Offset(0f, capY),
                end = Offset(w, capY),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            val count = monthLabels.size.coerceAtLeast(1)
            val slotW = w / count
            val barW = (slotW * 0.56f).coerceAtLeast(12f)

            for (i in 0 until count) {
                val base = baselineCents.getOrElse(i) { 0L }
                val add = addedCents.getOrElse(i) { 0L }
                val total = base + add
                val baseH = chartHeight * (base.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                val totalH = chartHeight * (total.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                val addH = (totalH - baseH).coerceAtLeast(0f)

                val x = i * slotW + (slotW - barW) / 2f
                val baseTop = chartTop + chartHeight - baseH
                val addTop = chartTop + chartHeight - totalH

                if (baseH > 1f) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF3B4266), Color(0xFF181D33))),
                        topLeft = Offset(x, baseTop),
                        size = Size(barW, baseH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                if (addH > 1f) {
                    val isOverCap = total > prudentCapCents
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            if (isOverCap) listOf(Color(0xFFFB7185), RoseHazard, Color(0xFF9F1239))
                            else listOf(LunarPrimary, LunarCyan, LunarDeep)
                        ),
                        topLeft = Offset(x, addTop),
                        size = Size(barW, addH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            monthLabels.forEachIndexed { idx, label ->
                val total = baselineCents.getOrElse(idx) { 0L } + addedCents.getOrElse(idx) { 0L }
                val over = total > prudentCapCents
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = OnSurfaceMuted
                    )
                    Text(
                        text = "${(total / 100L)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                        color = if (over) hazardColor else LunarCyan
                    )
                }
            }
        }
    }
}
