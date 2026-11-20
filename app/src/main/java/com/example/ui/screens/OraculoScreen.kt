package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DateUtils
import com.example.data.formatBrl
import com.example.data.repo.OracleAction
import com.example.domain.OracleResearchEngine
import com.example.domain.TermKey
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.RpgInsetWell
import com.example.ui.components.StatusChip
import com.example.ui.components.TermLabelWithTooltip
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary
import com.example.viewmodel.LedgerUiState
import com.example.viewmodel.OracleChatMessage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OraculoScreen(
    ledgerState: LedgerUiState.Ready,
    messages: List<OracleChatMessage>,
    isStreaming: Boolean,
    prefilledPrompt: String?,
    onConsumePrefilled: () -> Unit,
    onSendMessage: (String) -> Unit,
    onExecuteAction: (OracleAction) -> Unit,
    onClearChat: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = ledgerState.profile.languageLevel
    var inputText by remember { mutableStateOf("") }
    var showResearchRadar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val snapshot = remember(ledgerState) { ledgerState.toContextSnapshot() }
    val dossier = remember(snapshot) { OracleResearchEngine.computeStatisticalDossier(snapshot) }
    val liveSearchResult = remember(searchQuery, snapshot) {
        if (searchQuery.isBlank()) null
        else OracleResearchEngine.searchLedgerAndKnowledge(searchQuery, snapshot)
    }

    LaunchedEffect(prefilledPrompt) {
        if (!prefilledPrompt.isNullOrBlank()) {
            onSendMessage(prefilledPrompt)
            onConsumePrefilled()
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val quickPrompts = listOf(
        "Analise minha média, mediana e Pareto do mês",
        "Posso parcelar R$ 1.200 em 6x sem quebrar minha Build?",
        "Como Selic, CDI e inflação afetam minha reserva?",
        "Onde cortar gastos sem perder qualidade de vida?",
        "Explique Competência vs. Caixa no estilo RPG"
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    com.example.ui.components.SampledDrawableImage(
                        resId = R.drawable.img_oracle_orb_1794969169050,
                        contentDescription = "Orbe do Oráculo",
                        maxDimPx = 192,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, SolarMagenta.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    TermLabelWithTooltip(
                        termKey = TermKey.ORACLE_TITLE,
                        languageLevel = lang,
                        textColor = LunarPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showResearchRadar = !showResearchRadar },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("oracle_toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (showResearchRadar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Pesquisar no Livro e Economia",
                            tint = if (showResearchRadar) GoldBadge else LunarCyan
                        )
                    }
                    IconButton(
                        onClick = onClearChat,
                        enabled = !isStreaming && messages.size > 1,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("oracle_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Nova consulta",
                            tint = if (!isStreaming && messages.size > 1) LunarPrimary else OnSurfaceMuted.copy(alpha = 0.45f)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("oraculo_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = LunarCyan)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Finanças, Economia, Estatística & Estratégia RPG.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                StatusChip(
                    text = if (dossier.expenseCount > 0) "Mediana ${formatBrl(dossier.medianExpenseCents)}" else "Caixa ${formatBrl(ledgerState.profile.checkingBalanceCents)}",
                    color = EmeraldSafe
                )
            }

            // Expandable Radar de Pesquisa (Pesquisa no Livro-Razão & Enciclopédia Econômico-RPG)
            AnimatedVisibility(visible = showResearchRadar) {
                RpgInsetWell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    borderColor = GoldBadge.copy(alpha = 0.55f),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = GoldBadge, modifier = Modifier.size(16.dp))
                            Text(
                                text = "RADAR DE PESQUISA: LIVRO-RAZÃO & ECONOMIA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldBadge
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar gasto, #tag, Selic, CDI, Pareto, média...") },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpar busca", tint = OnSurfaceMuted)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldBadge,
                                unfocusedBorderColor = BevelBorderIndigo,
                                focusedContainerColor = MidnightBase,
                                unfocusedContainerColor = MidnightBase,
                                focusedTextColor = OnSurfaceText,
                                unfocusedTextColor = OnSurfaceText
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("oracle_search_input")
                        )

                        if (liveSearchResult != null) {
                            if (liveSearchResult.matchedTransactions.isNotEmpty()) {
                                Text(
                                    text = "${liveSearchResult.matchedTransactions.size} lançamento(s) • Total ${formatBrl(liveSearchResult.matchedTotalCents)} (média ${formatBrl(liveSearchResult.matchedMeanCents)})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LunarCyan
                                )
                                liveSearchResult.matchedTransactions.take(2).forEach { tx ->
                                    Text(
                                        text = "• ${DateUtils.epochDayToDisplay(tx.dateEpochDay)} — ${tx.description}: ${formatBrl(tx.amountCents)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "Nenhum lançamento exato com \"$searchQuery\" — pesquisando conceitos econômicos e estatísticos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceMuted
                                )
                            }

                            liveSearchResult.matchedTopics.firstOrNull()?.let { topic ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CosmicIndigo.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, LunarCyan.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "${topic.title} (${topic.domainLabel})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LunarPrimary
                                        )
                                        Text(
                                            text = topic.rpgMetaphor,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            LunarCoreButton(
                                text = "Pesquisar e Analisar \"$searchQuery\"",
                                enabled = !isStreaming,
                                onClick = {
                                    val q = searchQuery.trim()
                                    if (q.isNotEmpty()) {
                                        onSendMessage("Pesquise e analise no meu livro-razão e sob a ótica econômica/estatística: $q")
                                        searchQuery = ""
                                        showResearchRadar = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "oracle_search_ask_button"
                            )
                        } else {
                            Text(
                                text = "Temas rápidos da Biblioteca:",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OracleResearchEngine.encyclopedia.take(6).forEach { topic ->
                                    Surface(
                                        modifier = Modifier.clickable(enabled = !isStreaming) {
                                            onSendMessage("Explique ${topic.title} aplicando aos meus números reais de forma lúdica em RPG.")
                                            showResearchRadar = false
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MidnightBase,
                                        border = BorderStroke(1.dp, LunarCyan.copy(alpha = 0.45f))
                                    ) {
                                        Text(
                                            text = topic.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LunarPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Multi-turn Chat Messages Thread
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    OracleBubble(
                        message = msg,
                        onExecuteAction = onExecuteAction
                    )
                }
            }

            // Horizontal Scrollable Quick Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        modifier = Modifier.clickable(enabled = !isStreaming) { onSendMessage(prompt) },
                        shape = RoundedCornerShape(6.dp),
                        color = MidnightVoid,
                        border = BorderStroke(1.dp, BevelBorderIndigo)
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall,
                            color = LunarPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Input Bar cleanly anchored above navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Pergunte sobre finanças, economia, estatística ou RPG...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LunarCyan,
                        unfocusedBorderColor = BevelBorderIndigo,
                        focusedContainerColor = MidnightVoid,
                        unfocusedContainerColor = MidnightVoid,
                        focusedTextColor = OnSurfaceText,
                        unfocusedTextColor = OnSurfaceText
                    ),
                    modifier = Modifier.weight(1f).testTag("oracle_input")
                )
                IconButton(
                    onClick = {
                        val toSend = inputText.trim()
                        if (toSend.isNotEmpty()) {
                            inputText = ""
                            onSendMessage(toSend)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isStreaming,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (inputText.isNotBlank() && !isStreaming) LunarCyan else MidnightVoid)
                        .testTag("oracle_send_button")
                ) {
                    if (isStreaming) {
                        CircularProgressIndicator(
                            color = LunarCyan,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensagem",
                            tint = if (inputText.isNotBlank()) MidnightVoid else OnSurfaceMuted
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OracleBubble(
    message: OracleChatMessage,
    onExecuteAction: (OracleAction) -> Unit
) {
    val isUser = message.isUser
    val formattedText = remember(message.text) { formatOracleRichText(message.text) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        RpgBevelCard(
            borderColor = if (isUser) SolarMagenta.copy(alpha = 0.65f) else LunarCyan.copy(alpha = 0.6f),
            containerColor = if (isUser) CosmicIndigo else MidnightVoid.copy(alpha = 0.92f),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isUser) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LunarCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = if (isUser) "Você" else "O Oráculo • Mestre em Finanças & RPG",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isUser) SolarSecondary else LunarCyan
                )
            }

            if (!isUser && message.evidenceBadges.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    message.evidenceBadges.forEach { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CosmicIndigo.copy(alpha = 0.65f),
                            border = BorderStroke(0.8.dp, LunarCyan.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = LunarPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = formattedText,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceText
            )

            if (message.actions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    message.actions.forEach { action ->
                        LunarCoreButton(
                            text = action.label,
                            onClick = { onExecuteAction(action) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "oracle_action_chip"
                        )
                    }
                }
            }
        }
    }
}

private fun formatOracleRichText(raw: String): AnnotatedString = buildAnnotatedString {
    val boldOrItalicRegex = Regex("(\\*\\*[^*]+\\*\\*|_[^_]+_)")
    var cursor = 0
    for (match in boldOrItalicRegex.findAll(raw)) {
        if (match.range.first > cursor) {
            append(raw.substring(cursor, match.range.first))
        }
        val token = match.value
        when {
            token.startsWith("**") && token.endsWith("**") && token.length > 4 -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = LunarPrimary)) {
                    append(token.substring(2, token.length - 2))
                }
            }
            token.startsWith("_") && token.endsWith("_") && token.length > 2 -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = OnSurfaceMuted)) {
                    append(token.substring(1, token.length - 1))
                }
            }
            else -> append(token)
        }
        cursor = match.range.last + 1
    }
    if (cursor < raw.length) {
        append(raw.substring(cursor))
    }
}
