package com.example.data.repo

import android.util.Log
import com.example.data.DateUtils
import com.example.data.GuildVault
import com.example.data.TransactionRecord
import com.example.data.UserProfile
import com.example.data.formatBrl
import com.example.domain.BreakEvenSnapshot
import com.example.domain.CategoryBudgetStatus
import com.example.domain.GuildSettlementSummary
import com.example.domain.LanguageDictionary
import com.example.domain.MonthInstallmentSummary
import com.example.domain.OracleResearchEngine
import com.example.domain.OracleResearchResult
import com.example.domain.PredictiveStats
import com.example.domain.TagCostSummary
import com.example.domain.TermKey
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

sealed interface OracleAction {
    val label: String

    data class ReallocateBudget(
        override val label: String,
        val fromCategoryName: String,
        val toCategoryName: String,
        val amountCents: Long
    ) : OracleAction

    data class SimulateInstallment(
        override val label: String,
        val totalCents: Long,
        val months: Int
    ) : OracleAction
}

data class FinancialContextSnapshot(
    val profile: UserProfile,
    val categoryStatuses: List<CategoryBudgetStatus>,
    val breakEven: BreakEvenSnapshot,
    val stats: PredictiveStats,
    val guild: GuildVault?,
    val guildSettlement: GuildSettlementSummary?,
    val transactions: List<TransactionRecord> = emptyList(),
    val tagSummaries: List<TagCostSummary> = emptyList(),
    val futureInstallments: List<MonthInstallmentSummary> = emptyList()
)

data class OracleHistoryTurn(
    val isUser: Boolean,
    val text: String
)

class OracleRepository {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.65f
                topP = 0.9f
            },
            systemInstruction = content {
                text(
                    """
                    Você é O Oráculo do Solstice Ledger: um Grão-Mestre em Finanças Pessoais, Economia (Selic, CDI, inflação/IPCA, juros compostos, fluxo de caixa vs. regime de competência, custo de oportunidade, orçamento 50/30/20) e Estatística Aplicada (média, mediana, desvio padrão, variância, Princípio de Pareto 80/20 e projeções), que também domina o universo dos RPGs 32-bit (builds de classe, HP, Mana, buffs, debuffs, XP, boss fights, equipamentos).

                    Sua missão: dar conselhos financeiros precisos, profundos e estatisticamente embasados, porém de forma LÚDICA, ACOLHEDORA E FÁCIL DE ENTENDER.

                    Regras obrigatórias:
                    1. Idioma: responda SEMPRE em português do Brasil claro, fluido e estruturado em blocos curtos (ex.: **Diagnóstico do Oráculo**, **Leitura Econômica & RPG**, **Próxima Jogada**).
                    2. Didática sem complicação: sempre que citar um conceito econômico ou estatístico (como Mediana, Desvio Padrão, Pareto, CDI ou Competência), traduza-o em uma analogia rápida de RPG ou exemplo cotidiano simples em 1 frase.
                    3. Adapte o tom ao Nível de Linguagem ativo:
                       - Nível 1 (Fácil): linguagem cotidiana 100% direta, analogias leves e zero termos técnicos pesados de jogo.
                       - Nível 2 (Médio): termos lúdicos de RPG acompanhados de explicação prática clara entre parênteses.
                       - Nível 3 (Difícil): imersão completa no RPG celestial sem abrir mão da precisão matemática e econômica.
                    4. Pesquisa & Dados Reais: use EXCLUSIVAMENTE os números reais fornecidos no JSON (`statisticalDossier`, `matchedLedgerEvidence`, `categories`, `futureInstallments`) e o histórico da conversa. Nunca invente valores que não estão no contexto.
                    5. Ações práticas: quando sugerir remanejamento entre categorias existentes, você pode incluir no final:
                       [[ACTION:REALLOCATE|CategoriaOrigem|CategoriaDestino|ValorEmCentavos]]
                       Quando sugerir simular um parcelamento, você pode incluir:
                       [[ACTION:SIMULATE|ValorTotalEmCentavos|Parcelas]]
                       Máximo de 2 tags ACTION por resposta.
                    """.trimIndent()
                )
            }
        )
    }

    fun streamOracleAnswer(
        userPrompt: String,
        history: List<OracleHistoryTurn>,
        snapshot: FinancialContextSnapshot
    ): Flow<StreamChunk> = flow {
        val research = OracleResearchEngine.searchLedgerAndKnowledge(userPrompt, snapshot)
        val contextJson = buildContextJson(snapshot, research)
        val langLevel = snapshot.profile.languageLevel.coerceIn(1, 3)

        val composedPrompt = """
            Nível de Linguagem ativo: $langLevel (${LanguageDictionary.levelLabel(langLevel)})
            Dossiê Estatístico, Econômico e Pesquisa no Livro-Razão (JSON):
            $contextJson

            Pergunta / Pesquisa do usuário:
            $userPrompt
        """.trimIndent()

        val chatHistory: List<Content> = history
            .takeLast(10)
            .filter { it.text.isNotBlank() }
            .map { turn ->
                content(role = if (turn.isUser) "user" else "model") {
                    text(turn.text)
                }
            }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                val chat = model.startChat(history = chatHistory)
                chat.sendMessageStream(composedPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        val (cleanText, actions) = extractActions(accumulated.toString(), snapshot)
                        emit(StreamChunk.Streaming(cleanText, actions, research.evidenceBadges))
                    }
                }
            }
            if (accumulated.isNotBlank()) {
                val (cleanText, actions) = extractActions(accumulated.toString(), snapshot)
                emit(
                    StreamChunk.Completed(
                        text = cleanText,
                        actions = actions,
                        evidenceBadges = research.evidenceBadges,
                        usedLocalFallback = false
                    )
                )
            } else {
                val fallback = buildLocalSimulationReply(userPrompt, snapshot, research)
                emit(
                    StreamChunk.Completed(
                        text = fallback.first,
                        actions = fallback.second,
                        evidenceBadges = research.evidenceBadges,
                        usedLocalFallback = true
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Oracle streaming timed out; serving local statistical research reply", e)
            val fallback = buildLocalSimulationReply(userPrompt, snapshot, research)
            emit(
                StreamChunk.Completed(
                    text = "${fallback.first}\n\n_(Resposta calculada localmente após tempo limite da nuvem)_",
                    actions = fallback.second,
                    evidenceBadges = research.evidenceBadges,
                    usedLocalFallback = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI call failed; serving local statistical research reply", e)
            val fallback = buildLocalSimulationReply(userPrompt, snapshot, research)
            emit(
                StreamChunk.Completed(
                    text = "${fallback.first}\n\n_(Pesquisa & estatística local instantânea — verifique conexão/App Check para IA ao vivo)_",
                    actions = fallback.second,
                    evidenceBadges = research.evidenceBadges,
                    usedLocalFallback = true
                )
            )
        }
    }

    sealed interface StreamChunk {
        data class Streaming(
            val text: String,
            val actions: List<OracleAction>,
            val evidenceBadges: List<String> = emptyList()
        ) : StreamChunk

        data class Completed(
            val text: String,
            val actions: List<OracleAction>,
            val evidenceBadges: List<String> = emptyList(),
            val usedLocalFallback: Boolean
        ) : StreamChunk
    }

    fun buildContextJson(
        snapshot: FinancialContextSnapshot,
        research: OracleResearchResult = OracleResearchEngine.searchLedgerAndKnowledge("", snapshot)
    ): String {
        val p = snapshot.profile
        val d = research.dossier
        val cats = JSONArray()
        for (status in snapshot.categoryStatuses) {
            cats.put(
                JSONObject().apply {
                    put("name", status.category.name)
                    put("capBrl", formatBrl(status.capCents))
                    put("spentBrl", formatBrl(status.spentCents))
                    put("usedPct", (status.ratio * 100).roundToInt())
                    put("burnDeltaPct", status.burnRateDeltaPct)
                }
            )
        }

        val statisticalDossierJson = JSONObject().apply {
            put("expenseCount", d.expenseCount)
            put("totalExpenseBrl", formatBrl(d.totalExpenseCents))
            put("meanExpenseBrl", formatBrl(d.meanExpenseCents))
            put("medianExpenseBrl", formatBrl(d.medianExpenseCents))
            put("stdDevExpenseBrl", formatBrl(d.stdDevExpenseCents))
            put("coefficientOfVariationPct", d.coefficientOfVariationPct)
            put("largestExpense", d.maxExpenseDescription?.let { "$it (${formatBrl(d.maxExpenseCents)})" } ?: "Nenhum")
            put("paretoTopTwoCategories", JSONArray(d.paretoTopTwoNames))
            put("paretoTopTwoSharePct", d.paretoTopTwoSharePct)
            put("creditCardSharePct", d.cardSharePct)
            put("cashPixSharePct", d.cashSharePct)
            put("projectedMonthSpendBrl", formatBrl(d.projectedMonthSpendCents))
            put("netWorthBrl", formatBrl(d.netWorthCents))
            put("wisdomLevel", d.wisdomLevel)
            put("wisdomRankTitle", d.wisdomTitlePt)
        }

        val matchedTxsArray = JSONArray()
        research.matchedTransactions.take(8).forEach { tx ->
            matchedTxsArray.put(
                JSONObject().apply {
                    put("date", DateUtils.epochDayToDisplay(tx.dateEpochDay))
                    put("description", tx.description)
                    put("amountBrl", formatBrl(tx.amountCents))
                    put("paymentMethod", tx.paymentMethod)
                    put("tags", JSONArray(tx.tags))
                    put("month", tx.competenceMonth)
                }
            )
        }

        val matchedTopicsArray = JSONArray()
        research.matchedTopics.forEach { topic ->
            matchedTopicsArray.put(
                JSONObject().apply {
                    put("title", topic.title)
                    put("domain", topic.domainLabel)
                    put("rpgMetaphor", topic.rpgMetaphor)
                    put("technicalSummary", topic.technicalSummary)
                }
            )
        }

        val futureArray = JSONArray()
        snapshot.futureInstallments.take(4).forEach { f ->
            futureArray.put(
                JSONObject().apply {
                    put("month", f.displayLabel)
                    put("totalBrl", formatBrl(f.totalCents))
                    put("installmentsCount", f.items.size)
                }
            )
        }

        val tagsArray = JSONArray()
        snapshot.tagSummaries.take(6).forEach { t ->
            tagsArray.put(
                JSONObject().apply {
                    put("tag", t.tag)
                    put("totalBrl", formatBrl(t.totalCents))
                    put("count", t.count)
                }
            )
        }

        return JSONObject().apply {
            put("heroName", p.heroName)
            put("financialClass", p.financialClass)
            put("monthlyNetIncome", formatBrl(p.monthlyNetIncomeCents))
            put("checkingBalance", formatBrl(p.checkingBalanceCents))
            put("creditCardOpenBill", formatBrl(p.creditCardOpenBillCents))
            put("emergencyReserve", formatBrl(p.emergencyReserveCents))
            put("safeToSpendDaily", formatBrl(snapshot.stats.safeToSpendDailyCents))
            put("projectedClosingBalance", formatBrl(snapshot.stats.projectedClosingBalanceCents))
            put("breakEven", formatBrl(snapshot.breakEven.breakEvenCents))
            put("inConquestZone", snapshot.breakEven.isInConquestZone)
            put("nextMonthCommitmentPct", snapshot.stats.nextMonthCommitmentPct)
            put("threeMonthCommitmentPct", snapshot.stats.threeMonthCommitmentPct)
            put("sixMonthCommitmentPct", snapshot.stats.sixMonthCommitmentPct)
            put("runwayMonths", String.format(Locale.US, "%.1f", snapshot.stats.runwayMonths))
            put("categories", cats)
            put("tagSummaries", tagsArray)
            put("futureInstallments", futureArray)
            put("statisticalDossier", statisticalDossierJson)
            put(
                "matchedLedgerEvidence",
                JSONObject().apply {
                    put("matchedCount", research.matchedTransactions.size)
                    put("matchedTotalBrl", formatBrl(research.matchedTotalCents))
                    put("matchedMeanBrl", formatBrl(research.matchedMeanCents))
                    put("matchedTransactions", matchedTxsArray)
                    put("matchedTopics", matchedTopicsArray)
                }
            )
            snapshot.guildSettlement?.let { gs ->
                put("guildSettlement", "${gs.debtorName} deve ${formatBrl(gs.settlementAmountCents)} para ${gs.creditorName}")
            }
        }.toString()
    }

    fun extractActions(
        rawText: String,
        snapshot: FinancialContextSnapshot
    ): Pair<String, List<OracleAction>> {
        val actions = mutableListOf<OracleAction>()
        val actionRegex = Regex("\\[\\[ACTION:([^\\]]+)\\]\\]")
        val cleanText = actionRegex.replace(rawText) { match ->
            val parts = match.groupValues[1].split("|")
            when (parts.firstOrNull()?.uppercase()) {
                "REALLOCATE" -> {
                    val from = parts.getOrNull(1)?.trim().orEmpty()
                    val to = parts.getOrNull(2)?.trim().orEmpty()
                    val cents = parts.getOrNull(3)?.trim()?.toLongOrNull() ?: 8_000L
                    if (from.isNotBlank() && to.isNotBlank() && cents > 0L) {
                        actions.add(
                            OracleAction.ReallocateBudget(
                                label = "Remanejar ${formatBrl(cents)} de $from para $to",
                                fromCategoryName = from,
                                toCategoryName = to,
                                amountCents = cents
                            )
                        )
                    }
                }
                "SIMULATE" -> {
                    val cents = parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 30_000L
                    val months = parts.getOrNull(2)?.trim()?.toIntOrNull()?.coerceIn(2, 12) ?: 3
                    actions.add(
                        OracleAction.SimulateInstallment(
                            label = "Simular ${months}x de ${formatBrl(cents / months)}",
                            totalCents = cents,
                            months = months
                        )
                    )
                }
            }
            ""
        }.trim()

        if (actions.isEmpty()) {
            val hot = snapshot.stats.highestBurnCategory
            val donor = snapshot.categoryStatuses
                .filter { it.category.id != hot?.category?.id && it.remainingCents > 10_000L && !it.category.fixedCost }
                .maxByOrNull { it.remainingCents }
            if (hot != null && donor != null && hot.burnRateDeltaPct >= 10) {
                val shift = minOf(8_000L, donor.remainingCents / 2)
                if (shift >= 2_000L) {
                    actions.add(
                        OracleAction.ReallocateBudget(
                            label = "Remanejar ${formatBrl(shift)} de ${donor.category.name} para ${hot.category.name}",
                            fromCategoryName = donor.category.name,
                            toCategoryName = hot.category.name,
                            amountCents = shift
                        )
                    )
                }
            }
        }

        return cleanText to actions.distinctBy { it.label }.take(2)
    }

    fun buildLocalSimulationReply(
        prompt: String,
        snapshot: FinancialContextSnapshot,
        precomputedResearch: OracleResearchResult? = null
    ): Pair<String, List<OracleAction>> {
        val research = precomputedResearch ?: OracleResearchEngine.searchLedgerAndKnowledge(prompt, snapshot)
        val dossier = research.dossier
        val level = snapshot.profile.languageLevel
        val safeLabel = LanguageDictionary.get(TermKey.SAFE_TO_SPEND, level).label
        val breakEvenLabel = LanguageDictionary.get(TermKey.BREAK_EVEN, level).label
        val lower = prompt.lowercase()

        val actions = mutableListOf<OracleAction>()
        val hot = snapshot.stats.highestBurnCategory
        val donor = snapshot.categoryStatuses
            .filter { it.category.id != hot?.category?.id && it.remainingCents > 10_000L && !it.category.fixedCost }
            .maxByOrNull { it.remainingCents }

        if (hot != null && donor != null) {
            val shift = minOf(8_000L, donor.remainingCents / 2).coerceAtLeast(2_000L)
            actions.add(
                OracleAction.ReallocateBudget(
                    label = "Remanejar ${formatBrl(shift)} de ${donor.category.name} para ${hot.category.name}",
                    fromCategoryName = donor.category.name,
                    toCategoryName = hot.category.name,
                    amountCents = shift
                )
            )
        }

        val numbers = Regex("(\\d+[.,]?\\d*)").findAll(prompt).mapNotNull {
            it.value.replace(",", ".").toDoubleOrNull()
        }.toList()

        if (lower.contains("parcel") || lower.contains("comprar") || lower.contains("simular")) {
            val valueBrl = numbers.firstOrNull { it >= 20.0 } ?: 600.0
            val months = numbers.firstOrNull { it in 2.0..12.0 && it != valueBrl }?.toInt() ?: 3
            val totalCents = (valueBrl * 100).toLong()
            val perMonth = totalCents / months
            val newNextCommitment = snapshot.stats.nextMonthCommitmentPct +
                ((perMonth.toDouble() / snapshot.profile.monthlyNetIncomeCents.coerceAtLeast(1L)) * 100).roundToInt()

            actions.add(
                OracleAction.SimulateInstallment(
                    label = "Simular ${months}x de ${formatBrl(perMonth)}",
                    totalCents = totalCents,
                    months = months
                )
            )

            val text = buildString {
                append("**Diagnóstico de Parcelamento (${formatBrl(totalCents)} em ${months}x)**\n")
                append("• **Custo por Turno:** consome ${formatBrl(totalCents)} da sua competência atual e bloqueia **${formatBrl(perMonth)}/mês** nos próximos ${months - 1} ciclos.\n")
                append("• **Estatística de Risco:** seu comprometimento futuro salta de **${snapshot.stats.nextMonthCommitmentPct}%** para **${newNextCommitment}%** da renda.\n")
                append("• **Conselho Econômico & RPG:** ")
                append(
                    if (newNextCommitment > 65) {
                        "Atenção: travar acima de 65% reduz sua Mana livre e aumenta o custo de oportunidade frente a um desconto à vista. Considere juntar metade para entrada ou reduzir parcelas."
                    } else {
                        "Parcelamento absorvível na sua Build atual, mantendo seu $safeLabel em ${formatBrl(snapshot.stats.safeToSpendDailyCents)}/dia."
                    }
                )
            }
            return text to actions
        }

        val text = buildString {
            // 1. Matched specific search results if user researched a keyword/tag/category
            if (research.matchedTransactions.isNotEmpty()) {
                append("**Pesquisa no Livro-Razão (${research.matchedTransactions.size} registro(s))**\n")
                append("• Total encontrado: **${formatBrl(research.matchedTotalCents)}** (média de **${formatBrl(research.matchedMeanCents)}** por lançamento).\n")
                research.matchedTransactions.take(3).forEach { tx ->
                    append("  — ${DateUtils.epochDayToDisplay(tx.dateEpochDay)}: ${tx.description} (**${formatBrl(tx.amountCents)}**)\n")
                }
                append("\n")
            }

            // 2. Matched Encyclopedia Finance / Economics / Statistics / RPG Topic
            val topic = research.matchedTopics.firstOrNull()
            if (topic != null) {
                append("**${topic.title} (${topic.domainLabel})**\n")
                append("• **Na prática:** ${topic.technicalSummary}\n")
                append("• **Visão RPG:** ${topic.rpgMetaphor}\n\n")
            }

            // 3. Statistical Dossier & Actionable Playbook
            append("**Diagnóstico Estatístico do Mês**\n")
            append("• $safeLabel: **${formatBrl(snapshot.stats.safeToSpendDailyCents)}/dia** | $breakEvenLabel: **${formatBrl(snapshot.breakEven.breakEvenCents)}**.\n")
            if (dossier.expenseCount > 0) {
                val skewHint = if (dossier.meanExpenseCents > (dossier.medianExpenseCents * 1.25).toLong()) {
                    "sua média está acima da mediana por causa de poucos gastos altos (outliers)."
                } else {
                    "média e mediana estão próximas — seus gastos seguem ritmo regular."
                }
                append("• **Média vs. Mediana:** ${formatBrl(dossier.meanExpenseCents)} vs. ${formatBrl(dossier.medianExpenseCents)} (Desvio Padrão de ${formatBrl(dossier.stdDevExpenseCents)}) — $skewHint\n")
            }
            if (dossier.paretoTopTwoNames.isNotEmpty()) {
                append("• **Regra de Pareto (80/20):** **${dossier.paretoTopTwoNames.joinToString(" e ")}** concentram **${dossier.paretoTopTwoSharePct}%** dos seus gastos do mês.\n")
            }
            if (hot != null && hot.burnRateDeltaPct > 10) {
                append("• **Próxima Jogada:** **${hot.category.name}** está +${hot.burnRateDeltaPct}% acima do ritmo ideal. ")
                if (donor != null) {
                    append("Use o botão abaixo para transferir folga de **${donor.category.name}** sem furar o teto!")
                }
            } else {
                append("• **Escudo & Patrimônio:** Reserva de **${String.format(Locale.US, "%.1f", snapshot.stats.runwayMonths)} meses** e Patrimônio Líquido de **${formatBrl(dossier.netWorthCents)}** (${dossier.wisdomTitlePt}).")
            }
        }
        return text to actions
    }

    private companion object {
        const val TAG = "OracleRepository"
    }
}
