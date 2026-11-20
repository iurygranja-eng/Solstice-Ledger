package com.example.domain

import com.example.data.BudgetBucket
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.PaymentMethod
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.example.data.formatBrl
import com.example.data.safeEnumOf
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class PredictiveStats(
    val safeToSpendDailyCents: Long,
    val projectedClosingBalanceCents: Long,
    val nextMonthCommitmentPct: Int,
    val threeMonthCommitmentPct: Int,
    val sixMonthCommitmentPct: Int,
    val runwayMonths: Double,
    val monthsToSixMonthTarget: Int?,
    val projectedTargetMonthLabel: String?,
    val shieldTier: Int,
    val highestBurnCategory: CategoryBudgetStatus?,
    val dailyMission: DailyMissionCard
)

data class DailyMissionCard(
    val contextText: String,
    val tensionText: String,
    val actionText: String,
    val severity: MissionSeverity,
    val targetCategoryName: String?,
    val suggestedShiftCents: Long
)

enum class MissionSeverity {
    SAFE,
    CAUTION,
    RISK
}

data class GrimoireLesson(
    val id: String,
    val title: String,
    val badge: String,
    val summary: String,
    val keyTakeaway: String,
    val xpReward: Int
)

object StatsEngine {

    val allLessons: List<GrimoireLesson> = listOf(
        GrimoireLesson(
            id = "lesson_competencia",
            title = "Competência vs. Caixa",
            badge = "Fundamento",
            summary = "No cartão de crédito, o orçamento da categoria é consumido no dia da compra (Competência), mas o dinheiro só sai do banco quando você paga a fatura (Caixa).",
            keyTakeaway = "Pagar a fatura não duplica seus gastos nas categorias — é apenas uma transferência neutra.",
            xpReward = 50
        ),
        GrimoireLesson(
            id = "lesson_equilibrio",
            title = "O Ponto de Equilíbrio",
            badge = "Estratégia",
            summary = "Somar custos fixos, faturas e essenciais revela o valor exato que você precisa vencer no mês antes de entrar na Zona de Conquista.",
            keyTakeaway = "Tudo que entra acima da Linha de Equilíbrio acelera sua liberdade financeira.",
            xpReward = 75
        ),
        GrimoireLesson(
            id = "lesson_parcelamento",
            title = "Armadilha das Parcelas",
            badge = "Previsão",
            summary = "Parcelas pequenas parecem inofensivas hoje, mas se acumulam como 'Mana Bloqueada' reduzindo sua margem dos próximos 6 meses.",
            keyTakeaway = "Mantenha seu comprometimento futuro abaixo de 55% da renda líquida.",
            xpReward = 80
        ),
        GrimoireLesson(
            id = "lesson_neutralidade",
            title = "Neutralidade Patrimonial",
            badge = "Contabilidade",
            summary = "Mover dinheiro entre suas próprias contas ou acertar contas com o cônjuge muda onde o dinheiro está, nunca quanto você gastou.",
            keyTakeaway = "Use sempre Transferência Interna ou Acerto da Guilda para não distorcer relatórios.",
            xpReward = 60
        ),
        GrimoireLesson(
            id = "lesson_runway",
            title = "Escudo de 6 Meses",
            badge = "Fortaleza",
            summary = "O Horizonte de Segurança mede quantos meses seus custos essenciais sobrevivem sem nova renda. Seis meses transformam imprevistos em meros ajustes.",
            keyTakeaway = "Consistência mensal vale mais do que aportes esporádicos grandes.",
            xpReward = 100
        )
    )

    fun compute(
        profile: UserProfile,
        budgets: List<BudgetCategory>,
        transactions: List<TransactionRecord>,
        categoryStatuses: List<CategoryBudgetStatus>,
        month: String = AccountingEngine.getActiveCycleWindow(profile.cycleStartDay).cycleId
    ): PredictiveStats {
        val cycleWindow = AccountingEngine.getCycleWindow(month, profile.cycleStartDay)
        val dayOfMonth = cycleWindow.elapsedDays
        val daysInMonth = cycleWindow.totalDays
        val remainingDays = cycleWindow.remainingDays

        val monthExpenses = transactions.filter {
            AccountingEngine.effectiveTransactionCycle(it, profile.cycleStartDay) == month &&
                !AccountingEngine.isNeutralType(it.type) &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }

        val fixedCapCents = budgets
            .filter { it.fixedCost }
            .sumOf { AccountingEngine.percentageOfCents(profile.monthlyNetIncomeCents, it.percentageCap) }
        val fixedPaidSoFar = monthExpenses
            .filter { tx -> budgets.any { it.id == tx.categoryId && it.fixedCost } }
            .sumOf { it.amountCents }
        val pendingFixed = (fixedCapCents - fixedPaidSoFar).coerceAtLeast(0L)

        val variableSpentSoFar = monthExpenses
            .filter { tx -> budgets.none { it.id == tx.categoryId && it.fixedCost } }
            .sumOf { it.amountCents }

        val dailyVarAvg = if (dayOfMonth > 0) variableSpentSoFar.toDouble() / dayOfMonth else 0.0

        // 4. Limite Seguro Diário (Safe-to-Spend)
        val totalSpentThisMonth = monthExpenses.sumOf { it.amountCents }
        val availableForRestOfMonth = profile.monthlyNetIncomeCents -
            profile.savingsGoalCents -
            pendingFixed -
            totalSpentThisMonth
        val safeToSpend = (availableForRestOfMonth / remainingDays.coerceAtLeast(1)).coerceAtLeast(0L)

        // 2. Saldo Projetado de Fechamento
        val projectedRemainingVar = (dailyVarAvg * remainingDays).roundToLong()
        val projectedClose = profile.checkingBalanceCents -
            pendingFixed -
            profile.creditCardOpenBillCents -
            projectedRemainingVar

        // 3. Índice de Comprometimento Futuro (1, 3 e 6 meses)
        fun commitmentPctForMonths(horizon: Int): Int {
            val income = profile.monthlyNetIncomeCents.coerceAtLeast(1L) * horizon
            val recurringFixedTotal = fixedCapCents * horizon
            val futureInstallments = (1..horizon).sumOf { offset ->
                val targetMonth = DateUtils.addMonths(month, offset)
                transactions
                    .filter {
                        AccountingEngine.effectiveTransactionCycle(it, profile.cycleStartDay) == targetMonth &&
                            !AccountingEngine.isNeutralType(it.type) &&
                            safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE &&
                            safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD
                    }
                    .sumOf { it.amountCents }
            }
            return (((recurringFixedTotal + futureInstallments).toDouble() / income.toDouble()) * 100.0)
                .roundToInt()
                .coerceIn(0, 150)
        }

        val nextMonthPct = commitmentPctForMonths(1)
        val threeMonthPct = commitmentPctForMonths(3)
        val sixMonthPct = commitmentPctForMonths(6)

        // 5. Horizonte de Segurança (Runway)
        val essentialMonthlyCost = budgets
            .filter { safeEnumOf(it.bucket, BudgetBucket.ESSENTIAL) == BudgetBucket.ESSENTIAL }
            .sumOf { ((profile.monthlyNetIncomeCents * it.percentageCap) / 100.0).roundToLong() }
            .coerceAtLeast(100_000L)

        val runwayMonths = profile.emergencyReserveCents.toDouble() / essentialMonthlyCost.toDouble()
        val targetReserveCents = essentialMonthlyCost * 6L
        val deficitToSixMonths = (targetReserveCents - profile.emergencyReserveCents).coerceAtLeast(0L)
        val monthlySavingCapacity = profile.savingsGoalCents.coerceAtLeast(10_000L)
        val monthsToTarget = if (deficitToSixMonths == 0L) {
            0
        } else {
            ceil(deficitToSixMonths.toDouble() / monthlySavingCapacity.toDouble()).toInt().coerceAtMost(120)
        }
        val projectedMonthLabel = if (monthsToTarget == 0) {
            "Meta já alcançada!"
        } else {
            DateUtils.formatYearMonthShort(DateUtils.addMonths(month, monthsToTarget))
        }

        val shieldTier = when {
            runwayMonths >= 6.0 -> 5
            runwayMonths >= 4.0 -> 4
            runwayMonths >= 2.5 -> 3
            runwayMonths >= 1.0 -> 2
            else -> 1
        }

        // 1. Highest Burn Rate deviation for Daily Mission (Context -> Tension -> Action)
        val variableStatuses = categoryStatuses.filter { !it.category.fixedCost && it.category.bucket != BudgetBucket.RESERVE.name }
        val highestBurn = variableStatuses.maxByOrNull { it.burnRateDeltaPct }
        val monthElapsedPct = ((dayOfMonth.toDouble() / daysInMonth.coerceAtLeast(1)) * 100).roundToInt()

        val mission = buildDailyMission(
            highestBurn = highestBurn,
            monthElapsedPct = monthElapsedPct,
            safeToSpend = safeToSpend,
            nextMonthPct = nextMonthPct
        )

        return PredictiveStats(
            safeToSpendDailyCents = safeToSpend,
            projectedClosingBalanceCents = projectedClose,
            nextMonthCommitmentPct = nextMonthPct,
            threeMonthCommitmentPct = threeMonthPct,
            sixMonthCommitmentPct = sixMonthPct,
            runwayMonths = runwayMonths,
            monthsToSixMonthTarget = monthsToTarget,
            projectedTargetMonthLabel = projectedMonthLabel,
            shieldTier = shieldTier,
            highestBurnCategory = highestBurn,
            dailyMission = mission
        )
    }

    private fun buildDailyMission(
        highestBurn: CategoryBudgetStatus?,
        monthElapsedPct: Int,
        safeToSpend: Long,
        nextMonthPct: Int
    ): DailyMissionCard {
        if (highestBurn != null && highestBurn.burnRateDeltaPct >= 15) {
            val usedPct = (highestBurn.ratio * 100).roundToInt()
            val catName = highestBurn.category.name
            val estDayText = highestBurn.estimatedExhaustionDay?.let { "dia $it" } ?: "antes do fim do mês"
            val shift = (highestBurn.capCents * 0.12).roundToLong().coerceAtLeast(5_000L)
            return DailyMissionCard(
                contextText = "Você já usou $usedPct% de $catName com $monthElapsedPct% do mês decorrido.",
                tensionText = "Nesse ritmo, o orçamento dessa categoria pode acabar por volta do $estDayText.",
                actionText = "Segure gastos extras em $catName por 3 dias ou remaneje ${formatBrl(shift)} para fechar no azul.",
                severity = if (usedPct >= 90) MissionSeverity.RISK else MissionSeverity.CAUTION,
                targetCategoryName = catName,
                suggestedShiftCents = shift
            )
        }

        if (nextMonthPct > 65) {
            return DailyMissionCard(
                contextText = "Suas parcelas + custos fixos já comprometem $nextMonthPct% da renda do próximo mês.",
                tensionText = "Novas compras parceladas reduzirão seu limite diário nos próximos ciclos.",
                actionText = "Priorize compras à vista dentro do seu limite de ${formatBrl(safeToSpend)} hoje.",
                severity = MissionSeverity.CAUTION,
                targetCategoryName = null,
                suggestedShiftCents = 0L
            )
        }

        return DailyMissionCard(
            contextText = "Seu ritmo de gasto está equilibrado em $monthElapsedPct% do mês.",
            tensionText = "Manter essa cadência protege sua meta mensal e fortalece seu Escudo de Reserva.",
            actionText = "Mantenha o foco no limite seguro de ${formatBrl(safeToSpend)} hoje e avance para a Zona de Conquista.",
            severity = MissionSeverity.SAFE,
            targetCategoryName = null,
            suggestedShiftCents = 0L
        )
    }
}
