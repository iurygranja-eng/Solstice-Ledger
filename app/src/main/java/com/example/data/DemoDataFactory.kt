package com.example.data

import java.security.MessageDigest
import java.util.Locale

data class DemoSeedBundle(
    val profile: UserProfile,
    val budgets: List<Pair<String, BudgetCategory>>,
    val transactions: List<Pair<String, TransactionRecord>>,
    val guildId: String,
    val inviteCode: String,
    val guild: GuildVault,
    val guildEntries: List<Pair<String, GuildEntry>>,
    val accounts: List<Pair<String, BankAccount>> = emptyList()
)

object DemoDataFactory {

    fun buildDemoBundle(
        uid: String,
        heroName: String,
        avatarHair: Int,
        avatarOutfit: Int,
        avatarAura: String,
        financialClass: FinancialClass,
        monthlyNetIncomeCents: Long
    ): DemoSeedBundle {
        val currentMonth = DateUtils.currentYearMonth()
        val nextMonth = DateUtils.addMonths(currentMonth, 1)
        val monthPlus2 = DateUtils.addMonths(currentMonth, 2)
        val today = DateUtils.todayEpochDay()

        val guildId = "guild_${uid.take(10)}"
        val inviteCode = ("SOL" + uid.take(8)).uppercase(Locale.US).replace(Regex("[^A-Z0-9]"), "7")

        val savingsGoalCents = (monthlyNetIncomeCents * financialClass.reservePct) / 100L

        val budgets = createClassBudgets(uid, financialClass)

        // Realistic demo transactions showcasing:
        // 1. Fixed essential housing
        // 2. Variable food with high burn rate so Educational Mission triggers a helpful insight
        // 3. Credit card installment (1/3, 2/3, 3/3)
        // 4. Cross-cutting tags (#ViagemFérias, #Reforma, #Pet)
        // 5. Patrimonial neutral internal transfer
        val transactions = mutableListOf<Pair<String, TransactionRecord>>()

        fun addTx(
            id: String,
            desc: String,
            amountCents: Long,
            type: TransactionType,
            method: PaymentMethod,
            catId: String,
            tags: List<String>,
            dayOffset: Long,
            month: String = currentMonth,
            instIdx: Int = 1,
            instTotal: Int = 1,
            groupId: String = "",
            shared: Boolean = false
        ) {
            val epoch = (today - dayOffset).coerceAtLeast(1L)
            val hash = buildDedupeHash(epoch, amountCents, desc)
            transactions.add(
                id to TransactionRecord(
                    userId = uid,
                    description = desc,
                    amountCents = amountCents,
                    type = type.name,
                    paymentMethod = method.name,
                    categoryId = catId,
                    tags = tags,
                    dateEpochDay = epoch,
                    competenceMonth = month,
                    installmentIndex = instIdx,
                    installmentTotal = instTotal,
                    installmentGroupId = groupId,
                    dedupeHash = hash,
                    sharedGuild = shared,
                    paidByUid = uid
                )
            )
        }

        // Fixed housing & utilities (Debit/Pix)
        addTx(
            id = "demo_tx_moradia",
            desc = "Aluguel & Condomínio",
            amountCents = (monthlyNetIncomeCents * 0.25).toLong(),
            type = TransactionType.EXPENSE,
            method = PaymentMethod.DEBIT_PIX_CASH,
            catId = "cat_moradia",
            tags = listOf("#Casa"),
            dayOffset = 10,
            shared = true
        )
        addTx(
            id = "demo_tx_energia",
            desc = "Energia & Fibra Óptica",
            amountCents = 31_500L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.DEBIT_PIX_CASH,
            catId = "cat_moradia",
            tags = listOf("#Casa"),
            dayOffset = 7,
            shared = true
        )

        // Food & Delivery (shows lively burn rate in Alimentação)
        addTx(
            id = "demo_tx_mercado",
            desc = "Supermercado Estrela",
            amountCents = 64_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.DEBIT_PIX_CASH,
            catId = "cat_alimentacao",
            tags = listOf("#Casa"),
            dayOffset = 6,
            shared = true
        )
        addTx(
            id = "demo_tx_delivery",
            desc = "Delivery & Padaria",
            amountCents = 42_500L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_alimentacao",
            tags = emptyList(),
            dayOffset = 2
        )

        // Pet expense with #Pet tag
        addTx(
            id = "demo_tx_pet",
            desc = "Ração & Consulta Vet",
            amountCents = 28_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_saude_pet",
            tags = listOf("#Pet"),
            dayOffset = 4
        )

        // Credit card installment purchase (3x) for #ViagemFérias
        val groupTravel = "grp_viagem_ferias"
        addTx(
            id = "demo_tx_viagem_1",
            desc = "Passagens Solstício (1/3)",
            amountCents = 42_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_lazer",
            tags = listOf("#ViagemFérias"),
            dayOffset = 5,
            month = currentMonth,
            instIdx = 1,
            instTotal = 3,
            groupId = groupTravel
        )
        addTx(
            id = "demo_tx_viagem_2",
            desc = "Passagens Solstício (2/3)",
            amountCents = 42_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_lazer",
            tags = listOf("#ViagemFérias"),
            dayOffset = 5,
            month = nextMonth,
            instIdx = 2,
            instTotal = 3,
            groupId = groupTravel
        )
        addTx(
            id = "demo_tx_viagem_3",
            desc = "Passagens Solstício (3/3)",
            amountCents = 42_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_lazer",
            tags = listOf("#ViagemFérias"),
            dayOffset = 5,
            month = monthPlus2,
            instIdx = 3,
            instTotal = 3,
            groupId = groupTravel
        )

        // Home improvement #Reforma (2x)
        val groupReforma = "grp_reforma_estante"
        addTx(
            id = "demo_tx_reforma_1",
            desc = "Luminária & Pintura Sala (1/2)",
            amountCents = 26_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_lazer",
            tags = listOf("#Reforma"),
            dayOffset = 3,
            month = currentMonth,
            instIdx = 1,
            instTotal = 2,
            groupId = groupReforma,
            shared = true
        )
        addTx(
            id = "demo_tx_reforma_2",
            desc = "Luminária & Pintura Sala (2/2)",
            amountCents = 26_000L,
            type = TransactionType.EXPENSE,
            method = PaymentMethod.CREDIT_CARD,
            catId = "cat_lazer",
            tags = listOf("#Reforma"),
            dayOffset = 3,
            month = nextMonth,
            instIdx = 2,
            instTotal = 2,
            groupId = groupReforma,
            shared = true
        )

        // Neutral Internal Transfer (moving money to emergency reserve — 0 impact on expense caps)
        addTx(
            id = "demo_tx_reserva",
            desc = "Aporte Reserva Selic (Transferência)",
            amountCents = 65_000L,
            type = TransactionType.TRANSFERENCIA_INTERNA,
            method = PaymentMethod.DEBIT_PIX_CASH,
            catId = "cat_reserva",
            tags = listOf("#Escudo"),
            dayOffset = 8
        )

        // Open credit card bill = sum of current-month CREDIT_CARD expenses
        val openBill = transactions
            .filter {
                it.second.competenceMonth == currentMonth &&
                    it.second.paymentMethod == PaymentMethod.CREDIT_CARD.name &&
                    it.second.type == TransactionType.EXPENSE.name
            }
            .sumOf { it.second.amountCents }

        val profile = UserProfile(
            userId = uid,
            heroName = heroName,
            avatarHair = avatarHair,
            avatarOutfit = avatarOutfit,
            avatarAura = avatarAura,
            financialClass = financialClass.name,
            monthlyNetIncomeCents = monthlyNetIncomeCents,
            checkingBalanceCents = 342_000L,
            creditCardOpenBillCents = openBill,
            emergencyReserveCents = 1_450_000L,
            savingsGoalCents = savingsGoalCents,
            languageLevel = 2,
            privacyMode = PrivacyMode.CATEGORY_SUMMARY.name,
            guildId = guildId,
            xpPoints = 320L,
            hasSeenTour = false,
            hasSeenInstallmentTip = false,
            hasSeenTransferTip = false,
            hasSeenOfxTip = false,
            unlockedLessons = listOf("lesson_competencia", "lesson_equilibrio")
        )

        val guild = GuildVault(
            guildName = "Guilda Sol & Lua",
            inviteCode = inviteCode,
            ownerId = uid,
            memberUids = listOf(uid),
            memberNames = mapOf(uid to heroName),
            memberIncomesCents = mapOf(uid to monthlyNetIncomeCents),
            memberPrivacy = mapOf(uid to PrivacyMode.CATEGORY_SUMMARY.name),
            splitMode = GuildSplitMode.PROPORTIONAL.name,
            sharedGoalName = "Viagem & Reserva da Casa",
            sharedGoalTargetCents = 2_500_000L,
            sharedGoalSavedCents = 1_050_000L,
            companionDemoName = "Lyra (Companheira)",
            companionDemoIncomeCents = 4_800_00L,
            companionDemoPrivacy = PrivacyMode.CATEGORY_SUMMARY.name
        )

        val guildEntries = listOf(
            "entry_moradia" to GuildEntry(
                guildId = guildId,
                memberUids = listOf(uid),
                authorUid = uid,
                authorName = heroName,
                description = "Aluguel & Condomínio",
                amountCents = (monthlyNetIncomeCents * 0.25).toLong(),
                categoryId = "cat_moradia",
                entryType = GuildEntryType.SHARED_EXPENSE.name,
                dateEpochDay = (today - 10).coerceAtLeast(1L),
                competenceMonth = currentMonth
            ),
            "entry_mercado" to GuildEntry(
                guildId = guildId,
                memberUids = listOf(uid),
                authorUid = uid,
                authorName = heroName,
                description = "Supermercado Estrela",
                amountCents = 64_000L,
                categoryId = "cat_alimentacao",
                entryType = GuildEntryType.SHARED_EXPENSE.name,
                dateEpochDay = (today - 6).coerceAtLeast(1L),
                competenceMonth = currentMonth
            ),
            "entry_lyra_feirinha" to GuildEntry(
                guildId = guildId,
                memberUids = listOf(uid),
                authorUid = uid,
                authorName = "Lyra (Companheira)",
                description = "Feira Orgânica & Pet",
                amountCents = 48_000L,
                categoryId = "cat_alimentacao",
                entryType = GuildEntryType.SHARED_EXPENSE.name,
                dateEpochDay = (today - 4).coerceAtLeast(1L),
                competenceMonth = currentMonth
            )
        )

        val accounts = listOf(
            "acc_corrente" to BankAccount(
                userId = uid,
                name = "Bolsa Lunar (Corrente)",
                institution = "Banco Sol",
                accountType = BankAccountType.CHECKING.name,
                balanceCents = profile.checkingBalanceCents,
                colorHex = "#5EEAD4",
                iconKey = "WALLET",
                primaryAccount = true,
                includeInSafeToSpend = true,
                archived = false,
                sortOrder = 1
            ),
            "acc_reserva" to BankAccount(
                userId = uid,
                name = "Cofre Selic (Reserva)",
                institution = "Tesouro Estelar",
                accountType = BankAccountType.SAVINGS.name,
                balanceCents = profile.emergencyReserveCents,
                colorHex = "#34D399",
                iconKey = "SHIELD",
                primaryAccount = false,
                includeInSafeToSpend = false,
                archived = false,
                sortOrder = 2
            )
        )

        return DemoSeedBundle(
            profile = profile,
            budgets = budgets,
            transactions = transactions,
            guildId = guildId,
            inviteCode = inviteCode,
            guild = guild,
            guildEntries = guildEntries,
            accounts = accounts
        )
    }

    fun createClassBudgets(uid: String, financialClass: FinancialClass): List<Pair<String, BudgetCategory>> {
        val ess = financialClass.essentialPct.toDouble()
        val life = financialClass.lifestylePct.toDouble()
        val res = financialClass.reservePct.toDouble()

        return listOf(
            "cat_moradia" to BudgetCategory(
                userId = uid,
                name = "Moradia & Contas",
                iconKey = "HOME",
                bucket = BudgetBucket.ESSENTIAL.name,
                percentageCap = ess * 0.56,
                fixedCost = true,
                sortOrder = 1
            ),
            "cat_alimentacao" to BudgetCategory(
                userId = uid,
                name = "Alimentação",
                iconKey = "RESTAURANT",
                bucket = BudgetBucket.ESSENTIAL.name,
                percentageCap = ess * 0.30,
                fixedCost = false,
                sortOrder = 2
            ),
            "cat_saude_pet" to BudgetCategory(
                userId = uid,
                name = "Saúde & Transporte",
                iconKey = "HEALTH",
                bucket = BudgetBucket.ESSENTIAL.name,
                percentageCap = ess * 0.14,
                fixedCost = false,
                sortOrder = 3
            ),
            "cat_lazer" to BudgetCategory(
                userId = uid,
                name = "Lazer & Viagens",
                iconKey = "STAR",
                bucket = BudgetBucket.LIFESTYLE.name,
                percentageCap = life,
                fixedCost = false,
                sortOrder = 4
            ),
            "cat_reserva" to BudgetCategory(
                userId = uid,
                name = if (financialClass == FinancialClass.ALCHEMIST) "Quitação & Reserva" else "Reserva & Metas",
                iconKey = "SHIELD",
                bucket = BudgetBucket.RESERVE.name,
                percentageCap = res,
                fixedCost = false,
                sortOrder = 5
            )
        )
    }

    fun buildDedupeHash(epochDay: Long, amountCents: Long, description: String): String {
        val normalized = description.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
        val raw = "$epochDay|$amountCents|$normalized"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }
}
