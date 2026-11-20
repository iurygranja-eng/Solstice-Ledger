package com.example.domain

import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.PaymentMethod
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import com.example.data.repo.FinancialContextSnapshot
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class StatisticalDossier(
    val expenseCount: Int,
    val totalExpenseCents: Long,
    val meanExpenseCents: Long,
    val medianExpenseCents: Long,
    val stdDevExpenseCents: Long,
    val coefficientOfVariationPct: Int,
    val maxExpenseDescription: String?,
    val maxExpenseCents: Long,
    val paretoTopTwoNames: List<String>,
    val paretoTopTwoSharePct: Int,
    val cardSharePct: Int,
    val cashSharePct: Int,
    val projectedMonthSpendCents: Long,
    val netWorthCents: Long,
    val wisdomLevel: Int,
    val wisdomTitlePt: String
)

data class OracleKnowledgeTopic(
    val id: String,
    val title: String,
    val keywords: List<String>,
    val domainLabel: String,
    val rpgMetaphor: String,
    val technicalSummary: String
)

data class OracleResearchResult(
    val dossier: StatisticalDossier,
    val matchedTransactions: List<TransactionRecord>,
    val matchedTotalCents: Long,
    val matchedMeanCents: Long,
    val matchedCategories: List<CategoryBudgetStatus>,
    val matchedTags: List<TagCostSummary>,
    val matchedFutureInstallments: List<MonthInstallmentSummary>,
    val matchedTopics: List<OracleKnowledgeTopic>,
    val evidenceBadges: List<String>
)

object OracleResearchEngine {

    val encyclopedia: List<OracleKnowledgeTopic> = listOf(
        OracleKnowledgeTopic(
            id = "topic_juros_selic_cdi",
            title = "Juros Compostos, Selic & CDI",
            keywords = listOf("selic", "cdi", "juros", "rendimento", "invest", "cdb", "tesouro", "composto"),
            domainLabel = "Economia & Renda Fixa",
            rpgMetaphor = "Buff passivo de regeneração de Mana: cada moeda guardada gera novas moedas todo turno (mês), multiplicando seu poder sem gastar estamina extra.",
            technicalSummary = "A Selic é a taxa básica de juros da economia; o CDI acompanha de perto (~0,10 p.p. abaixo). Aplicações com liquidez diária atreladas a 100% do CDI protegem o poder de compra enquanto crescem exponencialmente via juros compostos."
        ),
        OracleKnowledgeTopic(
            id = "topic_inflacao",
            title = "Inflação & Poder de Compra",
            keywords = listOf("inflacao", "inflação", "ipca", "caro", "preco", "preço", "desvaloriza"),
            domainLabel = "Macroeconomia",
            rpgMetaphor = "Debuff invisível de veneno no ouro parado: se sua mochila de moedas não rende pelo menos a inflação (IPCA), ela compra menos poções a cada ano.",
            technicalSummary = "O ganho real é a diferença acima da inflação: (1 + taxa nominal) / (1 + inflação) - 1. Manter reservas em conta corrente sem rendimento gera perda real silenciosa."
        ),
        OracleKnowledgeTopic(
            id = "topic_estatistica_media_mediana",
            title = "Média, Mediana & Desvio Padrão",
            keywords = listOf("estat", "media", "média", "mediana", "desvio", "padrao", "padrão", "variancia", "variância", "volatilidade", "outlier"),
            domainLabel = "Estatística Aplicada",
            rpgMetaphor = "Média soma seus golpes normais com um acerto crítico raro; Mediana revela seu dano típico de todo turno; Desvio Padrão mede se seus gastos são estáveis ou caóticos.",
            technicalSummary = "Quando a Média supera muito a Mediana, poucos gastos grandes (outliers) estão distorcendo o mês. Reduzir o Desvio Padrão (coeficiente de variação) estabiliza o saldo projetado."
        ),
        OracleKnowledgeTopic(
            id = "topic_pareto",
            title = "Princípio de Pareto (80/20)",
            keywords = listOf("pareto", "concentra", "maior", "peso", "vilao", "vilão", "cortar", "economizar"),
            domainLabel = "Estatística & Eficiência",
            rpgMetaphor = "Foque seus feitiços nos 2 chefões (categorias) que drenam ~80% do HP em vez de caçar slimes insignificantes.",
            technicalSummary = "Na maioria dos orçamentos, 20% das categorias ou hábitos concentram cerca de 70%–80% dos desvios de meta. Ajustar o topo da distribuição traz alívio imediato."
        ),
        OracleKnowledgeTopic(
            id = "topic_competencia_caixa",
            title = "Competência vs. Caixa (Cartão)",
            keywords = listOf("competencia", "competência", "caixa", "cartao", "cartão", "fatura", "pix", "debito", "débito"),
            domainLabel = "Contabilidade Pessoal",
            rpgMetaphor = "Comprar no crédito reduz a Mana do orçamento na hora (Competência), mesmo que o dano no HP do banco só aconteça ao pagar a fatura (Caixa).",
            technicalSummary = "Separar competência de caixa evita a ilusão de saldo sobrando no banco e impede que o pagamento da fatura seja contado duas vezes como despesa."
        ),
        OracleKnowledgeTopic(
            id = "topic_parcelamento_custo_oportunidade",
            title = "Parcelamento & Custo de Oportunidade",
            keywords = listOf("parcel", "dividir", "avista", "à vista", "desconto", "oportunidade", "comprometer"),
            domainLabel = "Economia Comportamental",
            rpgMetaphor = "Cada parcela futura trava parte da sua Mana máxima dos próximos meses antes mesmo da batalha começar.",
            technicalSummary = "Compare sempre o desconto à vista contra o rendimento líquido que o capital teria no CDI. Parcelas acumuladas elevam o comprometimento futuro e reduzem flexibilidade estatística diante de imprevistos."
        ),
        OracleKnowledgeTopic(
            id = "topic_runway_escudo",
            title = "Reserva de Emergência (Runway)",
            keywords = listOf("reserva", "emergencia", "emergência", "runway", "escudo", "seguranca", "segurança", "meses"),
            domainLabel = "Gestão de Risco",
            rpgMetaphor = "É a barra de Escudo/Armadura do seu herói: quanto maior seu Runway (meses cobertos), menor a chance de entrar em dívida cara se surgir um Boss surpresa.",
            technicalSummary = "Runway = Reserva Líquida / Custo Mensal Essencial. Entre 3 e 6 meses de gastos essenciais garantem resiliência contra perda de renda ou despesas médicas."
        ),
        OracleKnowledgeTopic(
            id = "topic_regra_build_rpg",
            title = "Builds de Orçamento (50/30/20)",
            keywords = listOf("build", "classe", "50/30/20", "regra", "proporcao", "proporção", "orcamento", "orçamento", "teto"),
            domainLabel = "Planejamento Financeiro",
            rpgMetaphor = "Assim como você distribui pontos entre Defesa (Essenciais), Agilidade (Estilo de Vida) e Sabedoria (Reservas), sua classe define os tetos percentuais do mês.",
            technicalSummary = "Alocar cerca de 50% para essenciais, 30% para desejos e 20% para reservas/investimentos cria equilíbrio sustentável entre qualidade de vida hoje e patrimônio amanhã."
        ),
        OracleKnowledgeTopic(
            id = "topic_dividas_estrategia",
            title = "Bola de Neve vs. Avalanche",
            keywords = listOf("divida", "dívida", "quitar", "neve", "avalanche", "rotativo", "atraso", "pagar"),
            domainLabel = "Finanças & Comportamento",
            rpgMetaphor = "Avalanche derrota o chefe de maior dano (maior juros CET); Bola de Neve elimina os inimigos pequenos primeiro para ganhar XP moral rápido.",
            technicalSummary = "Matematicamente, o método Avalanche (maior taxa efetiva anual primeiro) economiza mais dinheiro; comportamentalmente, a Bola de Neve aumenta aderência por vitórias rápidas."
        ),
        OracleKnowledgeTopic(
            id = "topic_patrimonio_evolucao",
            title = "Patrimônio Líquido & Evolução",
            keywords = listOf("patrimonio", "patrimônio", "evolucao", "evolução", "avatar", "mascote", "traje", "sabedoria"),
            domainLabel = "Métricas Patrimoniais",
            rpgMetaphor = "Seu Patrimônio Líquido real (Caixa + Reserva − Fatura) forja trajes, relíquias e mascotes no seu perfil — riqueza é o que fica guardado, não o que foi exibido.",
            technicalSummary = "Patrimônio Líquido = Ativos Líquidos - Passivos de Curto Prazo. Aumentar reservas e reduzir faturas abertas eleva diretamente seu saldo patrimonial real."
        )
    )

    fun computeStatisticalDossier(
        snapshot: FinancialContextSnapshot,
        month: String = DateUtils.currentYearMonth()
    ): StatisticalDossier {
        val monthExpenses = snapshot.transactions.filter {
            it.competenceMonth == month &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }

        val amounts = monthExpenses.map { it.amountCents }.sorted()
        val count = amounts.size
        val total = amounts.sum()
        val mean = if (count > 0) total / count else 0L
        val median = when {
            count == 0 -> 0L
            count % 2 == 1 -> amounts[count / 2]
            else -> (amounts[count / 2 - 1] + amounts[count / 2]) / 2L
        }

        val stdDev = if (count > 1) {
            val meanD = mean.toDouble()
            val variance = amounts.sumOf { val diff = it.toDouble() - meanD; diff * diff } / count.toDouble()
            sqrt(variance).roundToLong()
        } else 0L

        val cvPct = if (mean > 0L) ((stdDev.toDouble() / mean.toDouble()) * 100.0).roundToInt() else 0
        val maxTx = monthExpenses.maxByOrNull { it.amountCents }

        val nonReserveCats = snapshot.categoryStatuses
            .filter { it.spentCents > 0L }
            .sortedByDescending { it.spentCents }
        val totalTrackedCatSpend = nonReserveCats.sumOf { it.spentCents }.coerceAtLeast(1L)
        val topTwo = nonReserveCats.take(2)
        val topTwoShare = if (nonReserveCats.isNotEmpty()) {
            ((topTwo.sumOf { it.spentCents }.toDouble() / totalTrackedCatSpend.toDouble()) * 100.0).roundToInt()
        } else 0

        val cardCents = monthExpenses
            .filter { safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD }
            .sumOf { it.amountCents }
        val cardPct = if (total > 0L) ((cardCents.toDouble() / total.toDouble()) * 100.0).roundToInt() else 0
        val cashPct = if (total > 0L) (100 - cardPct).coerceIn(0, 100) else 0

        val day = DateUtils.currentDayOfMonth().coerceAtLeast(1)
        val daysInMonth = DateUtils.daysInCurrentMonth().coerceAtLeast(1)
        val projectedMonthSpend = ((total.toDouble() / day.toDouble()) * daysInMonth.toDouble()).roundToLong()

        val evo = AvatarEvolutionEngine.compute(snapshot.profile)

        return StatisticalDossier(
            expenseCount = count,
            totalExpenseCents = total,
            meanExpenseCents = mean,
            medianExpenseCents = median,
            stdDevExpenseCents = stdDev,
            coefficientOfVariationPct = cvPct,
            maxExpenseDescription = maxTx?.description,
            maxExpenseCents = maxTx?.amountCents ?: 0L,
            paretoTopTwoNames = topTwo.map { it.category.name },
            paretoTopTwoSharePct = topTwoShare,
            cardSharePct = cardPct,
            cashSharePct = cashPct,
            projectedMonthSpendCents = projectedMonthSpend,
            netWorthCents = evo.netWorthCents,
            wisdomLevel = evo.wisdomLevel,
            wisdomTitlePt = evo.wisdomStage.titlePt
        )
    }

    fun searchLedgerAndKnowledge(
        query: String,
        snapshot: FinancialContextSnapshot,
        month: String = DateUtils.currentYearMonth()
    ): OracleResearchResult {
        val dossier = computeStatisticalDossier(snapshot, month)
        val normalized = normalizeForSearch(query)
        val tokens = normalized.split(Regex("\\s+"))
            .map { it.trim('#', ',', '.', '?', '!', ':', ';') }
            .filter { it.length >= 3 && it !in STOP_WORDS }

        val categoryNameById = snapshot.categoryStatuses.associate { it.category.id to it.category.name }

        val matchedTransactions = if (tokens.isEmpty()) {
            emptyList()
        } else {
            snapshot.transactions.filter { tx ->
                val catName = categoryNameById[tx.categoryId].orEmpty()
                val haystack = normalizeForSearch(
                    "${tx.description} $catName ${tx.tags.joinToString(" ")} ${tx.paymentMethod} ${tx.competenceMonth}"
                )
                tokens.any { tok -> haystack.contains(tok) }
            }.sortedByDescending { it.dateEpochDay }
        }

        val matchedCategories = if (tokens.isEmpty()) {
            emptyList()
        } else {
            snapshot.categoryStatuses.filter { cs ->
                val catHay = normalizeForSearch(cs.category.name)
                tokens.any { tok -> catHay.contains(tok) }
            }
        }

        val matchedTags = if (tokens.isEmpty()) {
            emptyList()
        } else {
            snapshot.tagSummaries.filter { ts ->
                val tagHay = normalizeForSearch(ts.tag)
                tokens.any { tok -> tagHay.contains(tok) }
            }
        }

        val matchedFutureInstallments = if (tokens.any {
                it.contains("parcel") || it.contains("fatur") || it.contains("futur") || it.contains("mes")
            }
        ) {
            snapshot.futureInstallments
        } else emptyList()

        val matchedTopics = encyclopedia.filter { topic ->
            val titleNorm = normalizeForSearch(topic.title)
            topic.keywords.any { kw -> normalized.contains(normalizeForSearch(kw)) } ||
                tokens.any { tok -> titleNorm.contains(tok) }
        }.take(2)

        val matchedTotal = matchedTransactions.sumOf { it.amountCents }
        val matchedMean = if (matchedTransactions.isNotEmpty()) matchedTotal / matchedTransactions.size else 0L

        val badges = buildList {
            if (matchedTransactions.isNotEmpty()) {
                add("Livro: ${matchedTransactions.size} lançamento(s) (${formatBrl(matchedTotal)})")
            }
            if (matchedTopics.isNotEmpty()) {
                add("Tema: ${matchedTopics.first().title}")
            }
            if (dossier.expenseCount > 0) {
                add("Média ${formatBrl(dossier.meanExpenseCents)} • Mediana ${formatBrl(dossier.medianExpenseCents)}")
            }
            if (dossier.paretoTopTwoNames.isNotEmpty()) {
                add("Pareto ${dossier.paretoTopTwoSharePct}% (${dossier.paretoTopTwoNames.joinToString(" + ")})")
            }
        }.take(3)

        return OracleResearchResult(
            dossier = dossier,
            matchedTransactions = matchedTransactions,
            matchedTotalCents = matchedTotal,
            matchedMeanCents = matchedMean,
            matchedCategories = matchedCategories,
            matchedTags = matchedTags,
            matchedFutureInstallments = matchedFutureInstallments,
            matchedTopics = matchedTopics,
            evidenceBadges = badges
        )
    }

    private fun normalizeForSearch(input: String): String =
        input.lowercase(Locale.ROOT)
            .replace("á", "a").replace("à", "a").replace("ã", "a").replace("â", "a")
            .replace("é", "e").replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o").replace("ô", "o").replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")

    private val STOP_WORDS = setOf(
        "como", "para", "com", "que", "qual", "quais", "meu", "minha", "meus", "minhas",
        "sobre", "pode", "posso", "devo", "fazer", "esta", "está", "isso", "esse", "essa",
        "uma", "uns", "das", "dos", "nos", "nas", "por", "mais", "menos", "hoje", "mes", "mês"
    )
}
