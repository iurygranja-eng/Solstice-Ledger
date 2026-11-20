package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetCategory
import com.example.data.PaymentMethod
import com.example.data.TransactionType
import com.example.data.formatBrl
import com.example.domain.LanguageDictionary
import com.example.domain.OfxCsvParser
import com.example.domain.ParsedImportItem
import com.example.domain.TermKey
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.RpgPsxWindow
import com.example.ui.components.SolarCompanionButton
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.PsxWindowBottom
import com.example.ui.theme.PsxWindowMid
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.StatCurrencyLarge
import com.example.ui.theme.StatCurrencySmall
import com.example.viewmodel.LedgerUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ForjaModalSheet(
    state: LedgerUiState.Ready,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmitManual: (
        description: String,
        amountCents: Long,
        type: TransactionType,
        paymentMethod: PaymentMethod,
        categoryId: String,
        tags: List<String>,
        installments: Int,
        sharedGuild: Boolean
    ) -> Unit,
    onConfirmImport: (List<ParsedImportItem>) -> Unit,
    onAskOracle: (String) -> Unit,
    initialTab: Int = 0
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val lang = state.profile.languageLevel

    var activeTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) } // 0 = Rápido, 1 = Simulador Parcelado, 2 = OFX/CSV
    var rawDigits by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.DEBIT_PIX_CASH) }
    var selectedCategoryId by remember {
        mutableStateOf(state.budgets.firstOrNull { !it.fixedCost }?.id ?: state.budgets.firstOrNull()?.id ?: "cat_alimentacao")
    }
    var installments by remember { mutableIntStateOf(1) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var sharedGuild by remember { mutableStateOf(false) }

    var parsedItems by remember { mutableStateOf<List<ParsedImportItem>>(emptyList()) }
    var importFileName by remember { mutableStateOf<String?>(null) }

    val existingHashes = remember(state.transactions) {
        state.transactions.mapNotNull { it.dedupeHash.takeIf { h -> h.isNotBlank() } }.toSet()
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val name = uri.lastPathSegment ?: "extrato.ofx"
            importFileName = name
            parsedItems = OfxCsvParser.parse(content, name, existingHashes)
        }
    }

    val amountCents = rawDigits.toLongOrNull() ?: 0L
    val activeStatusBadge = when (activeTab) {
        1 -> "SIMULAÇÃO"
        2 -> "EXTRATO"
        else -> "FORJA"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PsxWindowBottom,
        contentColor = OnSurfaceText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PsxWindowMid, PsxWindowBottom)))
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top 32-Bit Forja Context Header
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
                        imageVector = Icons.Default.Hardware,
                        contentDescription = null,
                        tint = LunarCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "FORJA // SIMULADOR ASTRAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = LunarPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(LunarCyan.copy(alpha = 0.14f))
                            .border(1.dp, LunarCyan.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(LunarCyan)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = activeStatusBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = LunarPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_forja_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar Forja", tint = OnSurfaceMuted)
                    }
                }
            }

            Text(
                text = LanguageDictionary.get(TermKey.FORJA_TITLE, lang).label,
                style = CinzelTitleStyle.copy(fontSize = 18.sp),
                color = LunarPrimary
            )

            // 3-Tab Mode Bar (Lançamento Rápido | ✦ Simulador Parcelado | Importar OFX/CSV)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MidnightVoid)
                    .border(1.dp, LunarCyan.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Lançamento", "✦ Simulador", "OFX / CSV").forEachIndexed { idx, title ->
                    val selected = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            LunarCyan.copy(alpha = 0.25f),
                                            CosmicIndigo.copy(alpha = 0.72f),
                                            LunarCyan.copy(alpha = 0.25f)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .then(
                                if (selected) {
                                    Modifier.border(1.dp, LunarCyan.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { activeTab = idx }
                            .testTag("forja_tab_$idx")
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (selected) LunarPrimary else OnSurfaceMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    RpgPsxWindow(
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Big Numeric Display (32-bit recessed bevel well)
                        RpgInsetWell(
                            borderColor = LunarCyan.copy(alpha = 0.65f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = formatBrl(amountCents),
                                style = StatCurrencyLarge,
                                color = if (amountCents > 0L) LunarPrimary else OnSurfaceMuted,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Transaction Type Pills
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                TransactionType.EXPENSE to "Gasto",
                                TransactionType.INCOME to "Receita",
                                TransactionType.TRANSFERENCIA_INTERNA to "Transf. Neutra",
                                TransactionType.PAGAMENTO_FATURA to "Pagar Fatura"
                            ).forEach { (type, label) ->
                                val selected = selectedType == type
                                Surface(
                                    modifier = Modifier
                                        .clickable {
                                            selectedType = type
                                            if (type != TransactionType.EXPENSE) {
                                                selectedMethod = PaymentMethod.DEBIT_PIX_CASH
                                                installments = 1
                                            }
                                        }
                                        .testTag("tx_type_${type.name.lowercase()}"),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selected) CosmicIndigo else MidnightVoid,
                                    border = BorderStroke(1.dp, if (selected) SolarMagenta else BevelBorderIndigo)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) SolarMagenta else OnSurfaceMuted,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        // Payment Method + Installments (only for Expense)
                        if (selectedType == TransactionType.EXPENSE) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isCash = selectedMethod == PaymentMethod.DEBIT_PIX_CASH
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedMethod = PaymentMethod.DEBIT_PIX_CASH
                                            installments = 1
                                        }
                                        .testTag("method_cash"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCash) CosmicIndigo else MidnightVoid,
                                    border = BorderStroke(1.5.dp, if (isCash) EmeraldSafe else BevelBorderIndigo)
                                ) {
                                    Text(
                                        text = "Conta (Caixa/Pix)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCash) EmeraldSafe else OnSurfaceMuted,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMethod = PaymentMethod.CREDIT_CARD }
                                        .testTag("method_credit"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (!isCash) CosmicIndigo else MidnightVoid,
                                    border = BorderStroke(1.5.dp, if (!isCash) SolarMagenta else BevelBorderIndigo)
                                ) {
                                    Text(
                                        text = "Cartão (Crédito)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (!isCash) SolarMagenta else OnSurfaceMuted,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            if (selectedMethod == PaymentMethod.CREDIT_CARD) {
                                Spacer(Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Parcelas:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnSurfaceMuted,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    listOf(1, 2, 3, 6, 10, 12).forEach { n ->
                                        val active = installments == n
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (active) SolarMagenta else MidnightVoid)
                                                .border(1.dp, if (active) SolarMagenta else BevelBorderIndigo, RoundedCornerShape(4.dp))
                                                .clickable { installments = n }
                                                .testTag("installments_$n")
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${n}x",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (active) Color.White else OnSurfaceText
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            // Big Category Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.budgets.forEach { cat: BudgetCategory ->
                                    val selected = selectedCategoryId == cat.id
                                    Surface(
                                        modifier = Modifier
                                            .clickable { selectedCategoryId = cat.id }
                                            .testTag("cat_chip_${cat.id}"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (selected) CosmicIndigo else MidnightVoid,
                                        border = BorderStroke(1.5.dp, if (selected) LunarCyan else BevelBorderIndigo)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = categoryIcon(cat.iconKey),
                                                contentDescription = null,
                                                tint = if (selected) LunarCyan else OnSurfaceMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) LunarPrimary else OnSurfaceText
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quick description + Tags + Guilda share
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 80) description = it },
                            placeholder = { Text("Descrição rápida (opcional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LunarCyan,
                                unfocusedBorderColor = BevelBorderIndigo,
                                focusedContainerColor = MidnightVoid,
                                unfocusedContainerColor = MidnightVoid,
                                focusedTextColor = OnSurfaceText,
                                unfocusedTextColor = OnSurfaceText
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("tx_description_input")
                        )

                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("#ViagemFérias", "#Reforma", "#Pet", "#Casa").forEach { tag ->
                                val active = tag in selectedTags
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedTags = if (active) selectedTags - tag else selectedTags + tag
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (active) LunarCyan.copy(alpha = 0.2f) else MidnightVoid,
                                    border = BorderStroke(1.dp, if (active) LunarCyan else BevelBorderIndigo)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) LunarPrimary else OnSurfaceMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (selectedType == TransactionType.EXPENSE) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sharedGuild = !sharedGuild },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = sharedGuild,
                                    onCheckedChange = { sharedGuild = it },
                                    colors = CheckboxDefaults.colors(checkedColor = SolarMagenta, uncheckedColor = BevelBorderIndigo)
                                )
                                Text("Compartilhar na Guilda (rateio do casal)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceText)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Compact Numeric Keypad
                        NumericKeypad(
                            onDigit = { d ->
                                if (rawDigits.length < 9) {
                                    rawDigits = if (rawDigits == "0") d else rawDigits + d
                                }
                            },
                            onBackspace = {
                                rawDigits = if (rawDigits.length <= 1) "0" else rawDigits.dropLast(1)
                            },
                            onClear = { rawDigits = "0" }
                        )

                        Spacer(Modifier.height(10.dp))

                        LunarCoreButton(
                            text = if (isSubmitting) "Salvando..." else "Registrar na Forja",
                            enabled = amountCents > 0L && !isSubmitting,
                            onClick = {
                                val fallbackDesc = description.ifBlank {
                                    when (selectedType) {
                                        TransactionType.INCOME -> "Receita Extra"
                                        TransactionType.TRANSFERENCIA_INTERNA -> "Transferência Interna"
                                        TransactionType.PAGAMENTO_FATURA -> "Pagamento de Fatura"
                                        TransactionType.ACERTO_GUILDA -> "Acerto Guilda"
                                        TransactionType.EXPENSE -> state.budgets.firstOrNull { it.id == selectedCategoryId }?.name ?: "Lançamento"
                                    }
                                }
                                onSubmitManual(
                                    fallbackDesc,
                                    amountCents,
                                    selectedType,
                                    selectedMethod,
                                    selectedCategoryId,
                                    selectedTags.toList(),
                                    installments,
                                    sharedGuild
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "submit_transaction_button"
                        )
                    }
                }

                1 -> {
                    InstallmentSimulatorSection(
                        state = state,
                        isSubmitting = isSubmitting,
                        onConfirmPurchase = { desc, cents, method, catId, tags, inst ->
                            onSubmitManual(
                                desc,
                                cents,
                                TransactionType.EXPENSE,
                                method,
                                catId,
                                tags,
                                inst,
                                false
                            )
                        },
                        onAskOracle = onAskOracle,
                        onDismiss = onDismiss
                    )
                }

                else -> {
                    // TAB 2: OFX / CSV Importer
                    RpgPsxWindow(
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Selecione um extrato .OFX ou .CSV do seu banco. Lançamentos idênticos são deduplicados automaticamente por hash e transferências internas são marcadas como neutras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LunarCoreButton(
                                text = "Escolher Arquivo",
                                onClick = { openDocLauncher.launch(arrayOf("*/*")) },
                                modifier = Modifier.weight(1f),
                                testTag = "pick_ofx_file_button"
                            )
                            SolarCompanionButton(
                                text = "Extrato Demo .OFX",
                                onClick = {
                                    importFileName = "extrato_demo_solstice.ofx"
                                    parsedItems = OfxCsvParser.parse(
                                        rawContent = OfxCsvParser.sampleDemoOfx(),
                                        fileName = "extrato_demo_solstice.ofx",
                                        existingHashes = existingHashes
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "load_demo_ofx_button"
                            )
                        }

                        if (importFileName != null) {
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Arquivo: $importFileName (${parsedItems.size} itens)", style = MaterialTheme.typography.labelMedium, color = LunarPrimary)
                            }
                        }

                        parsedItems.forEachIndexed { index, item ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = MidnightVoid,
                                border = BorderStroke(
                                    1.dp,
                                    if (item.duplicateInLedger) AmberWarn.copy(alpha = 0.5f) else BevelBorderIndigo
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !item.duplicateInLedger) {
                                            parsedItems = parsedItems.toMutableList().apply {
                                                this[index] = item.copy(selectedForImport = !item.selectedForImport)
                                            }
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.selectedForImport && !item.duplicateInLedger,
                                        enabled = !item.duplicateInLedger,
                                        onCheckedChange = { checked ->
                                            parsedItems = parsedItems.toMutableList().apply {
                                                this[index] = item.copy(selectedForImport = checked)
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceText)
                                        Text(
                                            text = if (item.duplicateInLedger) "Duplicata ignorada" else "${item.suggestedType.name} • ${item.suggestedCategoryId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.duplicateInLedger) AmberWarn else EmeraldSafe
                                        )
                                    }
                                    Text(
                                        text = formatBrl(item.amountCents),
                                        style = StatCurrencySmall,
                                        color = LunarPrimary
                                    )
                                }
                            }
                        }

                        if (parsedItems.any { it.selectedForImport && !it.duplicateInLedger }) {
                            Spacer(Modifier.height(10.dp))
                            LunarCoreButton(
                                text = if (isSubmitting) "Importando..." else "Confirmar Importação",
                                enabled = !isSubmitting,
                                onClick = { onConfirmImport(parsedItems) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "confirm_import_button"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.verticalGradient(listOf(CosmicIndigo.copy(alpha = 0.72f), MidnightBase))
                            )
                            .border(1.2.dp, BevelBorderIndigo, RoundedCornerShape(7.dp))
                            .clickable {
                                when (key) {
                                    "C" -> onClear()
                                    "DEL" -> onBackspace()
                                    else -> onDigit(key)
                                }
                            }
                            .testTag("keypad_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Apagar", tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = key,
                                style = StatCurrencySmall,
                                color = if (key == "C") SolarMagenta else OnSurfaceText
                            )
                        }
                    }
                }
            }
        }
    }
}
