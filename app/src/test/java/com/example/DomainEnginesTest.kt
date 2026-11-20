package com.example

import com.example.data.BudgetBucket
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.DemoDataFactory
import com.example.data.FinancialClass
import com.example.data.PaymentMethod
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.example.data.repo.FinancialContextSnapshot
import com.example.data.repo.OracleRepository
import com.example.domain.AccountingEngine
import com.example.domain.AvatarAccessoryTier
import com.example.domain.AvatarCostumeTier
import com.example.domain.AvatarEvolutionEngine
import com.example.domain.AvatarMascotTier
import com.example.domain.AvatarWisdomStage
import com.example.domain.NotificationMilestoneInput
import com.example.domain.OfxCsvParser
import com.example.domain.OracleResearchEngine
import com.example.domain.RpgNotificationEngine
import com.example.domain.RpgNotificationType
import com.example.domain.StatsEngine
import com.example.domain.mapWisdomLevelToVisualAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainEnginesTest {

    @Test
    fun `neutral types do not count toward category spending`() {
        val bundle = DemoDataFactory.buildDemoBundle(
            uid = "hero_1",
            heroName = "Zale",
            avatarHair = 0,
            avatarOutfit = 0,
            avatarAura = "CYAN",
            financialClass = FinancialClass.GUARDIAN,
            monthlyNetIncomeCents = 700_000L
        )
        val extraNeutral = TransactionRecord(
            userId = "hero_1",
            description = "Pagamento Fatura Cartao",
            amountCents = 150_000L,
            type = TransactionType.PAGAMENTO_FATURA.name,
            paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
            categoryId = "cat_reserva",
            competenceMonth = DateUtils.currentYearMonth()
        )
        val budgets = bundle.budgets.map { (id, b) -> b.copy(id = id) }
        val txsBefore = bundle.transactions.map { (id, t) -> t.copy(id = id) }
        val txsAfter = txsBefore + extraNeutral

        val statusesBefore = AccountingEngine.computeCategoryStatuses(bundle.profile, budgets, txsBefore)
        val statusesAfter = AccountingEngine.computeCategoryStatuses(bundle.profile, budgets, txsAfter)

        assertEquals(
            statusesBefore.sumOf { it.spentCents },
            statusesAfter.sumOf { it.spentCents }
        )
        assertTrue(AccountingEngine.isNeutralType(TransactionType.TRANSFERENCIA_INTERNA.name))
        assertTrue(AccountingEngine.isNeutralType(TransactionType.PAGAMENTO_FATURA.name))
        assertTrue(AccountingEngine.isNeutralType(TransactionType.ACERTO_GUILDA.name))
    }

    @Test
    fun `installments split across consecutive competence months`() {
        val base = TransactionRecord(
            userId = "hero_1",
            description = "Notebook Guilda",
            amountCents = 100_000L,
            type = TransactionType.EXPENSE.name,
            paymentMethod = PaymentMethod.CREDIT_CARD.name,
            categoryId = "cat_lazer",
            competenceMonth = "2026-11"
        )
        val installments = AccountingEngine.generateInstallments(base, 3)
        assertEquals(3, installments.size)
        assertEquals(100_000L, installments.sumOf { it.amountCents })
        assertEquals("2026-11", installments[0].competenceMonth)
        assertEquals("2026-12", installments[1].competenceMonth)
        assertEquals("2027-01", installments[2].competenceMonth)
        assertEquals(1, installments[0].installmentIndex)
        assertEquals(3, installments[2].installmentTotal)
    }

    @Test
    fun `demo guild settlement attributes companion entry correctly`() {
        val bundle = DemoDataFactory.buildDemoBundle(
            uid = "hero_1",
            heroName = "Valere",
            avatarHair = 1,
            avatarOutfit = 2,
            avatarAura = "MAGENTA",
            financialClass = FinancialClass.EXPLORER,
            monthlyNetIncomeCents = 600_000L
        )
        val entries = bundle.guildEntries.map { (id, e) -> e.copy(id = id) }
        val summary = AccountingEngine.computeGuildSettlement(
            guild = bundle.guild.copy(id = bundle.guildId),
            entries = entries,
            currentUid = "hero_1"
        )!!
        // Partner B (Lyra companion) paid R$ 480,00 (48_000 cents)
        assertEquals(48_000L, summary.partnerBPaidCents)
        assertTrue(summary.partnerAPaidCents > 0L)
        assertTrue(summary.totalSharedCents == summary.partnerAPaidCents + summary.partnerBPaidCents)
    }

    @Test
    fun `ofx parser detects internal transfers and deduplicates`() {
        val sample = OfxCsvParser.sampleDemoOfx()
        val firstPass = OfxCsvParser.parse(sample, "extrato.ofx", emptySet())
        assertEquals(3, firstPass.size)
        assertEquals(TransactionType.TRANSFERENCIA_INTERNA, firstPass[2].suggestedType)
        assertFalse(firstPass.any { it.duplicateInLedger })

        val existingHashes = setOf(firstPass[0].dedupeHash)
        val secondPass = OfxCsvParser.parse(sample, "extrato.ofx", existingHashes)
        assertTrue(secondPass[0].duplicateInLedger)
        assertFalse(secondPass[0].selectedForImport)
        assertFalse(secondPass[1].duplicateInLedger)
    }

    @Test
    fun `predictive stats compute runway and safe to spend`() {
        val bundle = DemoDataFactory.buildDemoBundle(
            uid = "hero_1",
            heroName = "Zale",
            avatarHair = 0,
            avatarOutfit = 0,
            avatarAura = "CYAN",
            financialClass = FinancialClass.GUARDIAN,
            monthlyNetIncomeCents = 800_000L
        )
        val budgets = bundle.budgets.map { (id, b) -> b.copy(id = id) }
        val txs = bundle.transactions.map { (id, t) -> t.copy(id = id) }
        val statuses = AccountingEngine.computeCategoryStatuses(bundle.profile, budgets, txs)
        val stats = StatsEngine.compute(bundle.profile, budgets, txs, statuses)

        assertTrue(stats.runwayMonths > 0.0)
        assertTrue(stats.safeToSpendDailyCents >= 0L)
        assertTrue(stats.shieldTier in 1..5)
    }

    @Test
    fun `avatar evolution scales with financial wisdom and net worth`() {
        val novice = UserProfile(
            xpPoints = 0L,
            checkingBalanceCents = 80_000L,
            emergencyReserveCents = 0L,
            creditCardOpenBillCents = 30_000L
        )
        val noviceEvo = AvatarEvolutionEngine.compute(novice)
        assertEquals(1, noviceEvo.wisdomLevel)
        assertEquals(AvatarWisdomStage.APRENDIZ, noviceEvo.wisdomStage)
        assertEquals(50_000L, noviceEvo.netWorthCents)
        assertEquals(AvatarCostumeTier.TRAVELER_TUNIC, noviceEvo.costume)
        assertEquals(AvatarAccessoryTier.COPPER_PIN, noviceEvo.accessory)
        assertEquals(AvatarMascotTier.SEALED_EGG, noviceEvo.mascot)
        assertEquals(50_000L, noviceEvo.nextMilestone?.missingCents)

        val guardian = UserProfile(
            xpPoints = 250L,
            checkingBalanceCents = 300_000L,
            emergencyReserveCents = 1_400_000L,
            creditCardOpenBillCents = 100_000L
        )
        val guardianEvo = AvatarEvolutionEngine.compute(guardian)
        assertEquals(3, guardianEvo.wisdomLevel)
        assertEquals(AvatarWisdomStage.GUARDIAO, guardianEvo.wisdomStage)
        assertEquals(1_600_000L, guardianEvo.netWorthCents)
        assertEquals(AvatarCostumeTier.MITHRIL_BRIGANDINE, guardianEvo.costume)
        assertEquals(AvatarAccessoryTier.CLARITY_DIADEM, guardianEvo.accessory)
        assertEquals(AvatarMascotTier.STAR_MOOGLE, guardianEvo.mascot)

        val archmage = UserProfile(
            xpPoints = 520L,
            checkingBalanceCents = 2_000_000L,
            emergencyReserveCents = 6_200_000L,
            creditCardOpenBillCents = 200_000L
        )
        val archmageEvo = AvatarEvolutionEngine.compute(archmage)
        assertEquals(6, archmageEvo.wisdomLevel)
        assertEquals(AvatarWisdomStage.ARQUIMAGO, archmageEvo.wisdomStage)
        assertEquals(AvatarCostumeTier.CELESTIAL_ARMOR, archmageEvo.costume)
        assertEquals(AvatarAccessoryTier.SOLAR_CROWN, archmageEvo.accessory)
        assertEquals(AvatarMascotTier.SOLSTICE_DRAGON, archmageEvo.mascot)
        assertNull(archmageEvo.nextMilestone)
    }

    @Test
    fun `oracle research engine computes descriptive statistics and finds ledger and economics topics`() {
        val bundle = DemoDataFactory.buildDemoBundle(
            uid = "hero_1",
            heroName = "Zale",
            avatarHair = 0,
            avatarOutfit = 0,
            avatarAura = "CYAN",
            financialClass = FinancialClass.GUARDIAN,
            monthlyNetIncomeCents = 800_000L
        )
        val budgets = bundle.budgets.map { (id, b) -> b.copy(id = id) }
        val txs = bundle.transactions.map { (id, t) -> t.copy(id = id) }
        val statuses = AccountingEngine.computeCategoryStatuses(bundle.profile, budgets, txs)
        val breakEven = AccountingEngine.computeBreakEven(bundle.profile, budgets, txs)
        val stats = StatsEngine.compute(bundle.profile, budgets, txs, statuses)
        val snapshot = FinancialContextSnapshot(
            profile = bundle.profile,
            categoryStatuses = statuses,
            breakEven = breakEven,
            stats = stats,
            guild = bundle.guild,
            guildSettlement = null,
            transactions = txs,
            tagSummaries = AccountingEngine.aggregateByTag(txs),
            futureInstallments = AccountingEngine.groupFutureInstallments(txs)
        )

        val dossier = OracleResearchEngine.computeStatisticalDossier(snapshot)
        assertTrue(dossier.expenseCount > 0)
        assertTrue(dossier.meanExpenseCents > 0L)
        assertTrue(dossier.medianExpenseCents > 0L)
        assertTrue(dossier.paretoTopTwoSharePct in 1..100)

        val research = OracleResearchEngine.searchLedgerAndKnowledge(
            query = "Como Selic, CDI e mediana ajudam no meu #Escudo?",
            snapshot = snapshot
        )
        assertTrue(research.matchedTopics.any { it.id == "topic_juros_selic_cdi" })
        assertTrue(research.matchedTopics.any { it.id == "topic_estatistica_media_mediana" })
        assertTrue(research.evidenceBadges.isNotEmpty())

        val (reply, _) = OracleRepository().buildLocalSimulationReply(
            prompt = "Analise minha média, mediana e Selic",
            snapshot = snapshot
        )
        assertTrue(reply.contains("Diagnóstico Estatístico"))
        assertTrue(reply.contains("Média vs. Mediana"))
    }

    @Test
    fun `wisdom level range mapping produces incremental visual assets`() {
        val lvl1 = mapWisdomLevelToVisualAssets(1)
        assertEquals(AvatarCostumeTier.TRAVELER_TUNIC, lvl1.costume)
        assertEquals(AvatarAccessoryTier.COPPER_PIN, lvl1.accessory)
        assertEquals(AvatarMascotTier.SEALED_EGG, lvl1.mascot)
        assertFalse(lvl1.auraRingEnabled)

        val lvl2 = mapWisdomLevelToVisualAssets(2)
        assertEquals(AvatarCostumeTier.LUNAR_MANTLE, lvl2.costume)
        assertEquals(AvatarAccessoryTier.CRYSTAL_AMULET, lvl2.accessory)
        assertEquals(AvatarMascotTier.LUNAR_SLIME, lvl2.mascot)
        assertTrue(lvl2.auraRingEnabled)

        val lvl4 = mapWisdomLevelToVisualAssets(4)
        assertEquals(AvatarCostumeTier.MITHRIL_BRIGANDINE, lvl4.costume)
        assertEquals(AvatarAccessoryTier.CLARITY_DIADEM, lvl4.accessory)
        assertEquals(AvatarMascotTier.STAR_MOOGLE, lvl4.mascot)
        assertEquals(AvatarWisdomStage.ESTRATEGA, lvl4.wisdomStage)

        val lvl5 = mapWisdomLevelToVisualAssets(5)
        assertEquals(AvatarCostumeTier.SOLSTICE_COAT, lvl5.costume)
        assertEquals(AvatarAccessoryTier.MANA_ORBITAL, lvl5.accessory)
        assertEquals(AvatarMascotTier.VAULT_OWL, lvl5.mascot)

        val lvl7 = mapWisdomLevelToVisualAssets(7)
        assertEquals(AvatarCostumeTier.CELESTIAL_ARMOR, lvl7.costume)
        assertEquals(AvatarAccessoryTier.SOLAR_CROWN, lvl7.accessory)
        assertEquals(AvatarMascotTier.SOLSTICE_DRAGON, lvl7.mascot)
    }

    @Test
    fun `rpg notification engine builds playful daily reminders across language levels`() {
        val lvl1 = RpgNotificationEngine.buildDailyReminderCopy(
            heroName = "Valere",
            safeToSpendDailyCents = 12_500L,
            languageLevel = 1,
            expensesLoggedToday = 0
        )
        assertEquals(RpgNotificationType.DAILY_EXPENSE_REMINDER, lvl1.type)
        assertEquals(RpgNotificationEngine.TARGET_FORJA, lvl1.openTarget)
        assertTrue(lvl1.title.contains("Valere"))
        assertTrue(lvl1.body.contains("125,00"))

        val lvl3 = RpgNotificationEngine.buildDailyReminderCopy(
            heroName = "Zale",
            safeToSpendDailyCents = 25_000L,
            languageLevel = 3,
            expensesLoggedToday = 2
        )
        assertTrue(lvl3.body.contains("2 feito(s)"))
        assertTrue(lvl3.body.contains("250,00"))
    }

    @Test
    fun `rpg notification engine detects achieved milestones and deduplicates already sent ids`() {
        val bundle = DemoDataFactory.buildDemoBundle(
            uid = "hero_1",
            heroName = "Garl",
            avatarHair = 0,
            avatarOutfit = 0,
            avatarAura = "GOLD",
            financialClass = FinancialClass.GUARDIAN,
            monthlyNetIncomeCents = 900_000L
        )
        val wealthyProfile = bundle.profile.copy(
            xpPoints = 220L, // Sabedoria Nv. 3
            emergencyReserveCents = 2_600_000L,
            savingsGoalCents = 1_500_000L,
            languageLevel = 3
        )
        val wealthyGuild = bundle.guild.copy(
            id = "guild_1",
            sharedGoalTargetCents = 1_000_000L,
            sharedGoalSavedCents = 1_050_000L
        )
        val budgets = bundle.budgets.map { (id, b) -> b.copy(id = id) }
        val txs = bundle.transactions.map { (id, t) -> t.copy(id = id) }
        val statuses = AccountingEngine.computeCategoryStatuses(wealthyProfile, budgets, txs)
        val breakEven = AccountingEngine.computeBreakEven(wealthyProfile, budgets, txs)
        val stats = StatsEngine.compute(wealthyProfile, budgets, txs, statuses)
        val evolution = AvatarEvolutionEngine.compute(wealthyProfile)

        val input = NotificationMilestoneInput(
            profile = wealthyProfile,
            breakEven = breakEven,
            stats = stats,
            evolution = evolution,
            guild = wealthyGuild
        )

        val firstPass = RpgNotificationEngine.detectAchievedMilestones(input, emptySet())
        assertTrue(firstPass.any { it.type == RpgNotificationType.RESERVE_SAVINGS_GOAL })
        assertTrue(firstPass.any { it.type == RpgNotificationType.WISDOM_LEVEL_UP })
        assertTrue(firstPass.any { it.type == RpgNotificationType.COSMETIC_UNLOCKED })
        assertTrue(firstPass.any { it.type == RpgNotificationType.GUILD_GOAL_REACHED })

        val alreadySent = firstPass.map { it.id }.toSet()
        val secondPass = RpgNotificationEngine.detectAchievedMilestones(input, alreadySent)
        assertTrue(secondPass.isEmpty())
    }

    @Test
    fun `accounting engine manages cycleStartDay window and competence card closing day in Long centavos`() {
        val beforeStart = DateUtils.parseToEpochDay(2026, 11, 3)
        val onStart = DateUtils.parseToEpochDay(2026, 11, 5)
        val afterClosing = DateUtils.parseToEpochDay(2026, 11, 22)

        assertEquals("2026-10", AccountingEngine.resolveCycleIdForDate(beforeStart, cycleStartDay = 5))
        assertEquals("2026-11", AccountingEngine.resolveCycleIdForDate(onStart, cycleStartDay = 5))

        val window = AccountingEngine.getCycleWindow("2026-11", cycleStartDay = 5, referenceEpochDay = onStart)
        assertEquals("05/11 – 04/12", window.label)
        assertEquals(30, window.totalDays)
        assertEquals(1, window.elapsedDays)
        assertEquals(30, window.remainingDays)

        // Credit card with closingDay = 20 shifts Nov 22 purchase into next competence cycle
        val shiftedCycle = AccountingEngine.resolveCompetenceCycle(
            purchaseEpochDay = afterClosing,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            cycleStartDay = 5,
            cardClosingDay = 20
        )
        assertEquals("2026-12", shiftedCycle)

        // Debit purchase on Nov 22 stays in current 2026-11 cycle
        val cashCycle = AccountingEngine.resolveCompetenceCycle(
            purchaseEpochDay = afterClosing,
            paymentMethod = PaymentMethod.DEBIT_PIX_CASH,
            cycleStartDay = 5,
            cardClosingDay = 20
        )
        assertEquals("2026-11", cashCycle)
    }

    @Test
    fun `accounting engine processes competence transactions and computes monthly break even in Long centavos`() {
        val cycleStartDay = 5
        val nov3 = DateUtils.parseToEpochDay(2026, 11, 3)   // belongs to 2026-10 cycle when cycleStartDay=5
        val nov10 = DateUtils.parseToEpochDay(2026, 11, 10) // belongs to 2026-11 cycle
        val nov12 = DateUtils.parseToEpochDay(2026, 11, 12) // belongs to 2026-11 cycle

        val profile = UserProfile(
            userId = "u1",
            monthlyNetIncomeCents = 600_000L, // R$ 6.000,00
            creditCardOpenBillCents = 50_000L, // R$ 500,00
            cycleStartDay = cycleStartDay
        )
        val budgets = listOf(
            BudgetCategory(
                id = "cat_fixed",
                userId = "u1",
                name = "Moradia",
                bucket = BudgetBucket.ESSENTIAL.name,
                percentageCap = 30.0, // R$ 1.800,00
                fixedCost = true,
                sortOrder = 0
            ),
            BudgetCategory(
                id = "cat_ess_var",
                userId = "u1",
                name = "Mercado",
                bucket = BudgetBucket.ESSENTIAL.name,
                percentageCap = 20.0, // R$ 1.200,00 (65% baseline = R$ 780,00)
                fixedCost = false,
                allowRollover = true,
                rolloverCarryCents = 15_000L, // +R$ 150,00 rollover
                sortOrder = 1
            )
        )
        val txs = listOf(
            // Previous cycle expense (Nov 3 < cycleStartDay 5) — excluded from 2026-11
            TransactionRecord(
                id = "t_prev",
                description = "Compra ciclo anterior",
                amountCents = 99_900L,
                type = TransactionType.EXPENSE.name,
                paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
                categoryId = "cat_ess_var",
                dateEpochDay = nov3,
                competenceMonth = "2026-11"
            ),
            // Fixed actual cost higher than 30% cap (R$ 2.000,00 vs R$ 1.800,00)
            TransactionRecord(
                id = "t_fixed",
                description = "Aluguel",
                amountCents = 200_000L,
                type = TransactionType.EXPENSE.name,
                paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
                categoryId = "cat_fixed",
                dateEpochDay = nov10,
                competenceMonth = "2026-11"
            ),
            // Credit card expense in 2026-11 (R$ 80,000 -> exceeds openBill 50_000)
            TransactionRecord(
                id = "t_card",
                description = "Compras Cartao",
                amountCents = 80_000L,
                type = TransactionType.EXPENSE.name,
                paymentMethod = PaymentMethod.CREDIT_CARD.name,
                categoryId = "cat_ess_var",
                dateEpochDay = nov12,
                competenceMonth = "2026-11"
            ),
            // Neutral transfer — must NOT count toward expenses or Break-Even
            TransactionRecord(
                id = "t_neutral",
                description = "Reserva Selic",
                amountCents = 120_000L,
                type = TransactionType.TRANSFERENCIA_INTERNA.name,
                paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
                categoryId = "cat_ess_var",
                dateEpochDay = nov12,
                competenceMonth = "2026-11"
            ),
            // Extra freelance income in 2026-11 (+R$ 500,00)
            TransactionRecord(
                id = "t_income",
                description = "Freela RPG",
                amountCents = 50_000L,
                type = TransactionType.INCOME.name,
                paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
                categoryId = "cat_ess_var",
                dateEpochDay = nov12,
                competenceMonth = "2026-11"
            )
        )

        val summary = AccountingEngine.processCompetenceTransactions(txs, "2026-11", cycleStartDay)
        assertEquals(50_000L, summary.totalIncomeCents)
        assertEquals(200_000L, summary.cashExpenseCents)
        assertEquals(80_000L, summary.creditCardExpenseCents)
        assertEquals(280_000L, summary.totalExpenseCents)
        assertEquals(120_000L, summary.neutralMovementCents)

        val statuses = AccountingEngine.computeCategoryStatuses(
            profile = profile,
            budgets = budgets,
            transactions = txs,
            month = "2026-11",
            dayOfMonth = 10,
            daysInMonth = 30
        )
        val marketStatus = statuses.first { it.category.id == "cat_ess_var" }
        // Base cap 20% of 600_000 = 120_000 + 15_000 rollover = 135_000 cents
        assertEquals(135_000L, marketStatus.capCents)
        // Only t_card (80_000) counts; t_prev (other cycle) and t_neutral (neutral) excluded
        assertEquals(80_000L, marketStatus.spentCents)

        val breakEven = AccountingEngine.computeBreakEven(profile, budgets, txs, month = "2026-11")
        // Effective income = 600_000 + 50_000 = 650_000
        assertEquals(650_000L, breakEven.monthlyIncomeCents)
        // Fixed = max(180_000, 200_000) = 200_000
        assertEquals(200_000L, breakEven.fixedCostsCents)
        // Bills/Installments = max(50_000, 80_000) = 80_000
        assertEquals(80_000L, breakEven.monthBillsAndInstallmentsCents)
        // Essential var baseline = 65% of 120_000 = 78_000
        assertEquals(78_000L, breakEven.essentialVariableEstimateCents)
        // BreakEven = 200_000 + 80_000 + 78_000 = 358_000
        assertEquals(358_000L, breakEven.breakEvenCents)
        assertTrue(breakEven.isInConquestZone)
        assertEquals(292_000L, breakEven.surplusOrDeficitCents)
    }
}
