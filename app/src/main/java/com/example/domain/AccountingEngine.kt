package com.example.domain

import com.example.data.BudgetBucket
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.GuildEntry
import com.example.data.GuildEntryType
import com.example.data.GuildSplitMode
import com.example.data.GuildVault
import com.example.data.PaymentMethod
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.example.data.safeEnumOf
import kotlin.math.abs
import kotlin.math.roundToLong

data class FinancialCycleWindow(
    val cycleId: String,
    val cycleStartDay: Int,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val totalDays: Int,
    val elapsedDays: Int,
    val remainingDays: Int,
    val progressRatio: Double,
    val label: String
)

data class CompetenceLedgerSummary(
    val cycleId: String,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val cashExpenseCents: Long,
    val creditCardExpenseCents: Long,
    val neutralMovementCents: Long,
    val netAccrualResultCents: Long,
    val transactionsInCycle: List<TransactionRecord>
)

data class CategoryBudgetStatus(
    val category: BudgetCategory,
    val capCents: Long,
    val spentCents: Long,
    val remainingCents: Long,
    val ratio: Float,
    val burnRateDeltaPct: Int,
    val estimatedExhaustionDay: Int?
)

data class BreakEvenSnapshot(
    val monthlyIncomeCents: Long,
    val fixedCostsCents: Long,
    val monthBillsAndInstallmentsCents: Long,
    val essentialVariableEstimateCents: Long,
    val breakEvenCents: Long,
    val isInConquestZone: Boolean,
    val surplusOrDeficitCents: Long,
    val cycleId: String = "",
    val baseMonthlyIncomeCents: Long = 0L,
    val extraIncomeCents: Long = 0L
)

data class TagCostSummary(
    val tag: String,
    val totalCents: Long,
    val count: Int
)

data class MonthInstallmentSummary(
    val yearMonth: String,
    val displayLabel: String,
    val totalCents: Long,
    val items: List<TransactionRecord>
)

data class GuildSettlementSummary(
    val totalSharedCents: Long,
    val partnerAPaidCents: Long,
    val partnerBPaidCents: Long,
    val partnerAShareRatio: Double,
    val partnerBShareRatio: Double,
    val partnerAName: String,
    val partnerBName: String,
    val partnerAUid: String,
    val partnerBUid: String,
    val debtorName: String,
    val creditorName: String,
    val debtorUid: String,
    val settlementAmountCents: Long
)

object AccountingEngine {

    /**
     * Normalizes the configurable cycle start day into the safe monthly range [1, 28].
     */
    fun normalizeCycleStartDay(cycleStartDay: Int): Int = cycleStartDay.coerceIn(1, 28)

    /**
     * Computes a percentage of an integer amount strictly in Long centavos using basis points (100 bps = 1%).
     */
    fun percentageOfCents(baseCents: Long, percentageCap: Double): Long {
        val safeBase = baseCents.coerceAtLeast(0L)
        val bps = (percentageCap.coerceIn(0.0, 100.0) * 100.0).roundToLong()
        return (safeBase * bps) / 10_000L
    }

    /**
     * Identifies patrimonial-neutral transaction types that shift balances between own accounts
     * or settle credit card bills/guild balances without inflating category spending.
     */
    fun isNeutralType(typeStr: String): Boolean {
        val type = safeEnumOf(typeStr, TransactionType.EXPENSE)
        return type == TransactionType.TRANSFERENCIA_INTERNA ||
            type == TransactionType.PAGAMENTO_FATURA ||
            type == TransactionType.ACERTO_GUILDA
    }

    /**
     * Resolves which financial cycle ID ("YYYY-MM") an epochDay belongs to, given [cycleStartDay].
     * Example: if cycleStartDay = 5, dates from 05/11 to 04/12 belong to cycle "2026-11",
     * while 03/11 belongs to cycle "2026-10".
     */
    fun resolveCycleIdForDate(
        epochDay: Long,
        cycleStartDay: Int = 1
    ): String {
        val startDay = normalizeCycleStartDay(cycleStartDay)
        val (year, month1to12, dayOfMonth) = DateUtils.epochDayToYearMonthDay(epochDay)
        val calendarYm = String.format(java.util.Locale.US, "%04d-%02d", year, month1to12)
        return if (dayOfMonth >= startDay) {
            calendarYm
        } else {
            DateUtils.addMonths(calendarYm, -1)
        }
    }

    /**
     * Resolves the accrual/competence cycle for a new purchase, honoring both [cycleStartDay]
     * and an optional credit card [cardClosingDay] (purchases on or after closingDay shift to the next cycle).
     */
    fun resolveCompetenceCycle(
        purchaseEpochDay: Long,
        paymentMethod: PaymentMethod,
        cycleStartDay: Int = 1,
        cardClosingDay: Int? = null
    ): String {
        val baseCycle = resolveCycleIdForDate(purchaseEpochDay, cycleStartDay)
        if (paymentMethod != PaymentMethod.CREDIT_CARD || cardClosingDay == null) {
            return baseCycle
        }
        val (_, _, purchaseDay) = DateUtils.epochDayToYearMonthDay(purchaseEpochDay)
        val normalizedClosing = cardClosingDay.coerceIn(1, 31)
        return if (purchaseDay >= normalizedClosing) {
            DateUtils.addMonths(baseCycle, 1)
        } else {
            baseCycle
        }
    }

    /**
     * Builds the exact start/end epoch days and elapsed/remaining day metrics for a given [cycleId] and [cycleStartDay].
     */
    fun getCycleWindow(
        cycleId: String,
        cycleStartDay: Int = 1,
        referenceEpochDay: Long = DateUtils.todayEpochDay()
    ): FinancialCycleWindow {
        val startDay = normalizeCycleStartDay(cycleStartDay)
        val (startYear, startMonth) = DateUtils.parseYearMonth(cycleId)
        val nextYm = DateUtils.addMonths(cycleId, 1)
        val (nextYear, nextMonth) = DateUtils.parseYearMonth(nextYm)

        val startEpochDay = DateUtils.parseToEpochDay(startYear, startMonth, startDay)
        val nextStartEpochDay = DateUtils.parseToEpochDay(nextYear, nextMonth, startDay)
        val endEpochDay = (nextStartEpochDay - 1L).coerceAtLeast(startEpochDay)
        val totalDays = (endEpochDay - startEpochDay + 1L).toInt().coerceAtLeast(1)

        val elapsedDays = when {
            referenceEpochDay < startEpochDay -> 1
            referenceEpochDay > endEpochDay -> totalDays
            else -> (referenceEpochDay - startEpochDay + 1L).toInt().coerceIn(1, totalDays)
        }
        val remainingDays = when {
            referenceEpochDay < startEpochDay -> totalDays
            referenceEpochDay > endEpochDay -> 1
            else -> (endEpochDay - referenceEpochDay + 1L).toInt().coerceAtLeast(1)
        }
        val progressRatio = (elapsedDays.toDouble() / totalDays.toDouble()).coerceIn(0.05, 1.0)
        val label = "${DateUtils.epochDayToDisplay(startEpochDay)} – ${DateUtils.epochDayToDisplay(endEpochDay)}"

        return FinancialCycleWindow(
            cycleId = cycleId,
            cycleStartDay = startDay,
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay,
            totalDays = totalDays,
            elapsedDays = elapsedDays,
            remainingDays = remainingDays,
            progressRatio = progressRatio,
            label = label
        )
    }

    /**
     * Returns the currently active [FinancialCycleWindow] for [referenceEpochDay] using [cycleStartDay].
     */
    fun getActiveCycleWindow(
        cycleStartDay: Int = 1,
        referenceEpochDay: Long = DateUtils.todayEpochDay()
    ): FinancialCycleWindow {
        val cycleId = resolveCycleIdForDate(referenceEpochDay, cycleStartDay)
        return getCycleWindow(cycleId, cycleStartDay, referenceEpochDay)
    }

    /**
     * Determines the effective competence cycle for a persisted [TransactionRecord].
     * - If the transaction is a multi-month installment or was explicitly shifted to a different competenceMonth
     *   (e.g., via card closingDay), honors [TransactionRecord.competenceMonth].
     * - Otherwise maps [TransactionRecord.dateEpochDay] through [resolveCycleIdForDate] with [cycleStartDay].
     */
    fun effectiveTransactionCycle(
        tx: TransactionRecord,
        cycleStartDay: Int = 1
    ): String {
        val startDay = normalizeCycleStartDay(cycleStartDay)
        if (startDay == 1) {
            return tx.competenceMonth.ifBlank { DateUtils.epochDayToYearMonth(tx.dateEpochDay) }
        }
        val dateCalendarYm = DateUtils.epochDayToYearMonth(tx.dateEpochDay)
        val wasExplicitlyShifted = tx.installmentTotal > 1 ||
            (tx.competenceMonth.isNotBlank() && tx.competenceMonth != dateCalendarYm)
        return if (wasExplicitlyShifted) {
            tx.competenceMonth
        } else {
            resolveCycleIdForDate(tx.dateEpochDay, startDay)
        }
    }

    /**
     * Processes all accrual/competence transactions belonging to [cycleId] using [cycleStartDay],
     * strictly separating INCOME, EXPENSE (cash vs. credit card), and neutral patrimonial transfers,
     * with all monetary totals maintained in integer centavos (Long).
     */
    fun processCompetenceTransactions(
        transactions: List<TransactionRecord>,
        cycleId: String,
        cycleStartDay: Int = 1
    ): CompetenceLedgerSummary {
        val cycleTxs = transactions.filter { effectiveTransactionCycle(it, cycleStartDay) == cycleId }

        var incomeCents = 0L
        var cashExpenseCents = 0L
        var creditCardExpenseCents = 0L
        var neutralCents = 0L

        for (tx in cycleTxs) {
            val amount = tx.amountCents.coerceAtLeast(0L)
            val txType = safeEnumOf(tx.type, TransactionType.EXPENSE)
            when {
                isNeutralType(tx.type) -> neutralCents += amount
                txType == TransactionType.INCOME -> incomeCents += amount
                txType == TransactionType.EXPENSE -> {
                    val method = safeEnumOf(tx.paymentMethod, PaymentMethod.DEBIT_PIX_CASH)
                    if (method == PaymentMethod.CREDIT_CARD) {
                        creditCardExpenseCents += amount
                    } else {
                        cashExpenseCents += amount
                    }
                }
            }
        }

        val totalExpenseCents = cashExpenseCents + creditCardExpenseCents
        return CompetenceLedgerSummary(
            cycleId = cycleId,
            totalIncomeCents = incomeCents,
            totalExpenseCents = totalExpenseCents,
            cashExpenseCents = cashExpenseCents,
            creditCardExpenseCents = creditCardExpenseCents,
            neutralMovementCents = neutralCents,
            netAccrualResultCents = incomeCents - totalExpenseCents,
            transactionsInCycle = cycleTxs
        )
    }

    fun computeCategoryStatuses(
        profile: UserProfile,
        budgets: List<BudgetCategory>,
        transactions: List<TransactionRecord>,
        month: String = getActiveCycleWindow(profile.cycleStartDay).cycleId,
        dayOfMonth: Int? = null,
        daysInMonth: Int? = null
    ): List<CategoryBudgetStatus> {
        val cycleWindow = getCycleWindow(month, profile.cycleStartDay)
        val elapsedDays = (dayOfMonth ?: cycleWindow.elapsedDays).coerceAtLeast(1)
        val totalCycleDays = (daysInMonth ?: cycleWindow.totalDays).coerceAtLeast(1)
        val cycleProgress = (elapsedDays.toDouble() / totalCycleDays.toDouble()).coerceIn(0.05, 1.0)

        val expenseTxs = processCompetenceTransactions(
            transactions = transactions,
            cycleId = month,
            cycleStartDay = profile.cycleStartDay
        ).transactionsInCycle.filter {
            !isNeutralType(it.type) &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }

        return budgets.sortedBy { it.sortOrder }.map { cat ->
            val baseCapCents = percentageOfCents(profile.monthlyNetIncomeCents, cat.percentageCap)
            val rolloverBonusCents = if (cat.allowRollover) cat.rolloverCarryCents else 0L
            val capCents = (baseCapCents + rolloverBonusCents).coerceAtLeast(100L)

            val spentCents = expenseTxs
                .filter { it.categoryId == cat.id || (cat.id.isBlank() && it.categoryId == "cat_${cat.sortOrder}") }
                .sumOf { it.amountCents }

            val ratio = (spentCents.toFloat() / capCents.toFloat()).coerceAtLeast(0f)
            val burnDeltaPct = ((ratio - cycleProgress) * 100.0).toInt()
            val dailyAvg = spentCents.toDouble() / elapsedDays.toDouble()
            val estDay = if (!cat.fixedCost && dailyAvg > 0.0 && spentCents < capCents) {
                val projected = (capCents / dailyAvg).toInt()
                if (projected in (elapsedDays + 1)..totalCycleDays) projected else null
            } else if (!cat.fixedCost && spentCents >= capCents) {
                elapsedDays
            } else {
                null
            }

            CategoryBudgetStatus(
                category = cat,
                capCents = capCents,
                spentCents = spentCents,
                remainingCents = capCents - spentCents,
                ratio = ratio,
                burnRateDeltaPct = burnDeltaPct,
                estimatedExhaustionDay = estDay
            )
        }
    }

    /**
     * Computes the monthly Break-Even point ("Ponto de Equilíbrio") for the active cycle,
     * summing fixed costs + credit card bills/installments + essential variable baseline,
     * strictly using integer centavos (Long).
     */
    fun computeBreakEven(
        profile: UserProfile,
        budgets: List<BudgetCategory>,
        transactions: List<TransactionRecord>,
        month: String = getActiveCycleWindow(profile.cycleStartDay).cycleId
    ): BreakEvenSnapshot {
        val summary = processCompetenceTransactions(
            transactions = transactions,
            cycleId = month,
            cycleStartDay = profile.cycleStartDay
        )
        val extraIncome = summary.totalIncomeCents
        val baseIncome = profile.monthlyNetIncomeCents.coerceAtLeast(0L)
        val effectiveIncome = baseIncome + extraIncome

        val fixedBudgetIds = budgets.filter { it.fixedCost }.map { it.id }.toSet()
        val essentialVariableIds = budgets
            .filter { !it.fixedCost && safeEnumOf(it.bucket, BudgetBucket.ESSENTIAL) == BudgetBucket.ESSENTIAL }
            .map { it.id }
            .toSet()

        val monthExpenses = summary.transactionsInCycle.filter {
            !isNeutralType(it.type) &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }

        // Fixed costs from fixed categories (use actual or capped fixed baseline, strictly in Long centavos)
        val fixedCapCents = budgets
            .filter { it.fixedCost }
            .sumOf { percentageOfCents(baseIncome, it.percentageCap) }
        val fixedActualCents = monthExpenses
            .filter { it.categoryId in fixedBudgetIds }
            .sumOf { it.amountCents }
        val fixedCosts = maxOf(fixedCapCents, fixedActualCents)

        // Open credit card bill + installments in competence cycle
        val monthCardInstallments = monthExpenses
            .filter { safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD }
            .sumOf { it.amountCents }
        val billsAndInstallments = maxOf(profile.creditCardOpenBillCents.coerceAtLeast(0L), monthCardInstallments)

        // Essential variable (debit/cash to avoid double counting card items already in bill)
        val essentialVarDebit = monthExpenses
            .filter {
                it.categoryId in essentialVariableIds &&
                    safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.DEBIT_PIX_CASH
            }
            .sumOf { it.amountCents }
        val essentialVarCap = budgets
            .filter { it.id in essentialVariableIds }
            .sumOf { percentageOfCents(baseIncome, it.percentageCap) }
        val essentialVarBaseline = (essentialVarCap * 65L) / 100L
        val essentialVarEstimate = if (essentialVarDebit > 0L) {
            maxOf(essentialVarDebit, essentialVarBaseline)
        } else {
            essentialVarBaseline
        }

        val breakEven = fixedCosts + billsAndInstallments + essentialVarEstimate
        val surplusOrDeficit = effectiveIncome - breakEven

        return BreakEvenSnapshot(
            monthlyIncomeCents = effectiveIncome,
            fixedCostsCents = fixedCosts,
            monthBillsAndInstallmentsCents = billsAndInstallments,
            essentialVariableEstimateCents = essentialVarEstimate,
            breakEvenCents = breakEven,
            isInConquestZone = surplusOrDeficit >= 0L,
            surplusOrDeficitCents = surplusOrDeficit,
            cycleId = month,
            baseMonthlyIncomeCents = baseIncome,
            extraIncomeCents = extraIncome
        )
    }

    fun aggregateByTag(transactions: List<TransactionRecord>): List<TagCostSummary> {
        val expenses = transactions.filter {
            !isNeutralType(it.type) &&
                safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE
        }
        val map = linkedMapOf<String, MutableList<Long>>()
        for (tx in expenses) {
            for (rawTag in tx.tags) {
                val tag = if (rawTag.startsWith("#")) rawTag else "#$rawTag"
                map.getOrPut(tag) { mutableListOf() }.add(tx.amountCents)
            }
        }
        return map.entries
            .map { (tag, amounts) ->
                TagCostSummary(tag = tag, totalCents = amounts.sum(), count = amounts.size)
            }
            .sortedByDescending { it.totalCents }
    }

    fun groupFutureInstallments(
        transactions: List<TransactionRecord>,
        currentMonth: String = DateUtils.currentYearMonth(),
        cycleStartDay: Int = 1
    ): List<MonthInstallmentSummary> {
        val months = (0..5).map { DateUtils.addMonths(currentMonth, it) }
        return months.mapNotNull { ym ->
            val items = transactions.filter {
                effectiveTransactionCycle(it, cycleStartDay) == ym &&
                    safeEnumOf(it.type, TransactionType.EXPENSE) == TransactionType.EXPENSE &&
                    safeEnumOf(it.paymentMethod, PaymentMethod.DEBIT_PIX_CASH) == PaymentMethod.CREDIT_CARD
            }
            if (items.isEmpty()) null
            else MonthInstallmentSummary(
                yearMonth = ym,
                displayLabel = DateUtils.formatYearMonthShort(ym),
                totalCents = items.sumOf { it.amountCents },
                items = items
            )
        }
    }

    fun computeGuildSettlement(
        guild: GuildVault?,
        entries: List<GuildEntry>,
        currentUid: String,
        month: String = DateUtils.currentYearMonth()
    ): GuildSettlementSummary? {
        if (guild == null) return null

        val partnerAUid = guild.memberUids.firstOrNull() ?: currentUid
        val partnerBUid = if (guild.memberUids.size >= 2) guild.memberUids[1] else "companion_demo"

        val partnerAName = guild.memberNames[partnerAUid] ?: "Você"
        val partnerBName = guild.memberNames[partnerBUid] ?: guild.companionDemoName ?: "Parceiro(a)"

        val incomeA = (guild.memberIncomesCents[partnerAUid] ?: 500_000L).coerceAtLeast(1L)
        val incomeB = (guild.memberIncomesCents[partnerBUid] ?: guild.companionDemoIncomeCents ?: 500_000L).coerceAtLeast(1L)

        val splitMode = safeEnumOf(guild.splitMode, GuildSplitMode.PROPORTIONAL)
        val ratioA = if (splitMode == GuildSplitMode.FIFTY_FIFTY) {
            0.5
        } else {
            incomeA.toDouble() / (incomeA + incomeB).toDouble()
        }
        val ratioB = 1.0 - ratioA

        val monthEntries = entries.filter { it.competenceMonth == month }
        val sharedExpenses = monthEntries.filter {
            safeEnumOf(it.entryType, GuildEntryType.SHARED_EXPENSE) == GuildEntryType.SHARED_EXPENSE
        }
        val settlements = monthEntries.filter {
            safeEnumOf(it.entryType, GuildEntryType.ACERTO_GUILDA) == GuildEntryType.ACERTO_GUILDA
        }

        val totalShared = sharedExpenses.sumOf { it.amountCents }
        val isSingleMemberDemo = guild.memberUids.size == 1 && !guild.companionDemoName.isNullOrBlank()
        val isPartnerBEntry: (GuildEntry) -> Boolean = { entry ->
            entry.authorUid == partnerBUid ||
                (isSingleMemberDemo && entry.authorName == guild.companionDemoName)
        }
        var paidB = sharedExpenses.filter(isPartnerBEntry).sumOf { it.amountCents }
        var paidA = sharedExpenses.filter { it.authorUid == partnerAUid && !isPartnerBEntry(it) }.sumOf { it.amountCents }

        // Apply recorded settlements
        for (s in settlements) {
            if (!isPartnerBEntry(s) && s.authorUid == partnerAUid) {
                paidA += s.amountCents
                paidB -= s.amountCents
            } else {
                paidB += s.amountCents
                paidA -= s.amountCents
            }
        }

        val expectedA = (totalShared * ratioA).roundToLong()
        val balanceA = paidA - expectedA // positive => A overpaid, B owes A

        val settlementAmount = abs(balanceA)
        val (debtorUid, debtorName, creditorName) = if (balanceA >= 0) {
            Triple(partnerBUid, partnerBName, partnerAName)
        } else {
            Triple(partnerAUid, partnerAName, partnerBName)
        }

        return GuildSettlementSummary(
            totalSharedCents = totalShared,
            partnerAPaidCents = paidA.coerceAtLeast(0L),
            partnerBPaidCents = paidB.coerceAtLeast(0L),
            partnerAShareRatio = ratioA,
            partnerBShareRatio = ratioB,
            partnerAName = partnerAName,
            partnerBName = partnerBName,
            partnerAUid = partnerAUid,
            partnerBUid = partnerBUid,
            debtorName = debtorName,
            creditorName = creditorName,
            debtorUid = debtorUid,
            settlementAmountCents = settlementAmount
        )
    }

    fun generateInstallments(
        base: TransactionRecord,
        installments: Int,
        cycleStartDay: Int = 1,
        cardClosingDay: Int? = null
    ): List<TransactionRecord> {
        val count = installments.coerceIn(1, 24)
        val baseCycle = if (cardClosingDay != null) {
            resolveCompetenceCycle(
                purchaseEpochDay = base.dateEpochDay,
                paymentMethod = safeEnumOf(base.paymentMethod, PaymentMethod.CREDIT_CARD),
                cycleStartDay = cycleStartDay,
                cardClosingDay = cardClosingDay
            )
        } else {
            base.competenceMonth.ifBlank { resolveCycleIdForDate(base.dateEpochDay, cycleStartDay) }
        }
        val seededBase = base.copy(competenceMonth = baseCycle)
        if (count == 1) return listOf(seededBase)

        val perInstallment = seededBase.amountCents / count
        val remainder = seededBase.amountCents % count
        val groupId = "grp_${System.currentTimeMillis()}_${abs(seededBase.description.hashCode())}"

        return (1..count).map { index ->
            val month = DateUtils.addMonths(baseCycle, index - 1)
            val cents = if (index == 1) perInstallment + remainder else perInstallment
            seededBase.copy(
                description = "${seededBase.description} ($index/$count)",
                amountCents = cents,
                competenceMonth = month,
                installmentIndex = index,
                installmentTotal = count,
                installmentGroupId = groupId
            )
        }
    }
}
