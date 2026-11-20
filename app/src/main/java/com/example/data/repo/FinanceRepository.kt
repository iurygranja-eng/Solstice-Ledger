package com.example.data.repo

import android.content.Context
import com.example.R
import com.example.data.BankAccount
import com.example.data.BankAccountType
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.DemoDataFactory
import com.example.data.FinancialClass
import com.example.data.GuildEntry
import com.example.data.GuildEntryType
import com.example.data.GuildSplitMode
import com.example.data.GuildVault
import com.example.data.OperationType
import com.example.data.PaymentMethod
import com.example.data.PrivacyMode
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.example.data.handleFirestoreError
import com.example.domain.AccountingEngine
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FinanceRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Usuário precisa estar autenticado com Google.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeBudgets(userId: String): Flow<List<BudgetCategory>> =
        db.collection("users").document(userId).collection("budgets")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BudgetCategory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }

    fun observeBankAccounts(userId: String): Flow<List<BankAccount>> =
        db.collection("users").document(userId).collection("accounts")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BankAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }

    fun observeTransactions(userId: String): Flow<List<TransactionRecord>> =
        db.collection("users").document(userId).collection("transactions")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TransactionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }

    fun observeGuildsForUser(userId: String): Flow<List<GuildVault>> =
        db.collection("guilds")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GuildVault::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeGuildEntries(guildId: String, userId: String): Flow<List<GuildEntry>> =
        db.collection("guilds").document(guildId).collection("entries")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GuildEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }

    suspend fun createHeroAndSeedDemo(
        heroName: String,
        avatarHair: Int,
        avatarOutfit: Int,
        avatarAura: String,
        financialClass: FinancialClass,
        monthlyNetIncomeCents: Long,
        includeDemoData: Boolean = true
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Não autenticado"))
        val cleanName = heroName.trim().ifBlank { "Viajante" }.take(40)
        val safeIncome = monthlyNetIncomeCents.coerceIn(100_00L, 100_000_000_00L)

        val bundle = DemoDataFactory.buildDemoBundle(
            uid = uid,
            heroName = cleanName,
            avatarHair = avatarHair.coerceIn(0, 10),
            avatarOutfit = avatarOutfit.coerceIn(0, 10),
            avatarAura = avatarAura,
            financialClass = financialClass,
            monthlyNetIncomeCents = safeIncome
        )

        val profileToSave = if (includeDemoData) {
            bundle.profile
        } else {
            bundle.profile.copy(
                checkingBalanceCents = safeIncome,
                creditCardOpenBillCents = 0L,
                emergencyReserveCents = 0L,
                xpPoints = 50L
            )
        }

        return try {
            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.set(userRef, profileToMap(profileToSave, isCreate = true))

            for ((budgetId, budget) in bundle.budgets) {
                val budgetRef = userRef.collection("budgets").document(budgetId)
                batch.set(budgetRef, budgetToMap(budget, isCreate = true))
            }

            for ((accountId, account) in bundle.accounts) {
                val seededAccount = if (includeDemoData) {
                    account
                } else {
                    account.copy(
                        balanceCents = if (account.accountTypeEnum() == BankAccountType.CHECKING) safeIncome else 0L
                    )
                }
                val accountRef = userRef.collection("accounts").document(accountId)
                batch.set(accountRef, seededAccount.toFirestoreMap(isCreate = true))
            }

            if (includeDemoData) {
                for ((txId, tx) in bundle.transactions) {
                    val txRef = userRef.collection("transactions").document(txId)
                    batch.set(txRef, txToMap(tx, isCreate = true))
                }
            }

            val guildRef = db.collection("guilds").document(bundle.guildId)
            batch.set(guildRef, guildToMap(bundle.guild, isCreate = true))

            val inviteRef = db.collection("guildInvites").document(bundle.inviteCode)
            batch.set(
                inviteRef,
                mapOf(
                    "guildId" to bundle.guildId,
                    "ownerId" to uid,
                    "guildName" to bundle.guild.guildName,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            if (includeDemoData) {
                for ((entryId, entry) in bundle.guildEntries) {
                    val entryRef = guildRef.collection("entries").document(entryId)
                    batch.set(entryRef, guildEntryToMap(entry, isCreate = true))
                }
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun addTransactionWithInstallments(
        profile: UserProfile,
        guild: GuildVault?,
        description: String,
        amountCents: Long,
        type: TransactionType,
        paymentMethod: PaymentMethod,
        categoryId: String,
        tags: List<String>,
        installments: Int,
        sharedGuild: Boolean
    ): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}/transactions"
        return try {
            val uid = requireUserId()
            require(amountCents in 1L..100_000_000_00L) { "Valor deve ser entre R$ 0,01 e R$ 1.000.000.000,00." }
            val cleanDesc = description.trim().ifBlank { "Lançamento" }.take(100)
            val cleanTags = tags.map { it.trim().take(24) }.filter { it.isNotBlank() }.take(10)
            val today = DateUtils.todayEpochDay()
            val currentMonth = DateUtils.currentYearMonth()

            val baseTx = TransactionRecord(
                userId = uid,
                description = cleanDesc,
                amountCents = amountCents,
                type = type.name,
                paymentMethod = paymentMethod.name,
                categoryId = categoryId.ifBlank { "cat_alimentacao" },
                tags = cleanTags,
                dateEpochDay = today,
                competenceMonth = currentMonth,
                installmentIndex = 1,
                installmentTotal = 1,
                installmentGroupId = "",
                dedupeHash = DemoDataFactory.buildDedupeHash(today, amountCents, cleanDesc),
                sharedGuild = sharedGuild,
                paidByUid = uid
            )

            val records = if (type == TransactionType.EXPENSE && paymentMethod == PaymentMethod.CREDIT_CARD && installments > 1) {
                AccountingEngine.generateInstallments(baseTx, installments)
            } else {
                listOf(baseTx)
            }

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)

            for (record in records) {
                val txId = "tx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                val txRef = userRef.collection("transactions").document(txId)
                batch.set(txRef, txToMap(record, isCreate = true))
            }

            // Cash vs Accrual balance updates + XP gain
            var newChecking = profile.checkingBalanceCents
            var newOpenBill = profile.creditCardOpenBillCents
            var newReserve = profile.emergencyReserveCents

            when (type) {
                TransactionType.EXPENSE -> {
                    if (paymentMethod == PaymentMethod.DEBIT_PIX_CASH) {
                        newChecking -= amountCents
                    } else {
                        // Only first installment lands in current month's open credit card bill
                        newOpenBill += records.first().amountCents
                    }
                }
                TransactionType.INCOME -> {
                    newChecking += amountCents
                }
                TransactionType.TRANSFERENCIA_INTERNA -> {
                    newChecking -= amountCents
                    newReserve += amountCents
                }
                TransactionType.PAGAMENTO_FATURA -> {
                    newChecking -= amountCents
                    newOpenBill = (newOpenBill - amountCents).coerceAtLeast(0L)
                }
                TransactionType.ACERTO_GUILDA -> {
                    newChecking -= amountCents
                }
            }

            val profileUpdates = mutableMapOf<String, Any>(
                "checkingBalanceCents" to newChecking,
                "creditCardOpenBillCents" to newOpenBill,
                "emergencyReserveCents" to newReserve,
                "xpPoints" to (profile.xpPoints + 15L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (type == TransactionType.EXPENSE && paymentMethod == PaymentMethod.CREDIT_CARD && installments > 1 && !profile.hasSeenInstallmentTip) {
                profileUpdates["hasSeenInstallmentTip"] = true
            }
            if (AccountingEngine.isNeutralType(type.name) && !profile.hasSeenTransferTip) {
                profileUpdates["hasSeenTransferTip"] = true
            }

            batch.update(userRef, profileUpdates)

            // If shared in Guilda, also mirror entry into Guilda vault
            if (sharedGuild && guild != null && guild.id.isNotBlank() && type == TransactionType.EXPENSE) {
                val entryId = "entry_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                val entryRef = db.collection("guilds").document(guild.id).collection("entries").document(entryId)
                val entry = GuildEntry(
                    guildId = guild.id,
                    memberUids = guild.memberUids,
                    authorUid = uid,
                    authorName = profile.heroName,
                    description = cleanDesc,
                    amountCents = amountCents,
                    categoryId = categoryId,
                    entryType = GuildEntryType.SHARED_EXPENSE.name,
                    dateEpochDay = today,
                    competenceMonth = currentMonth
                )
                batch.set(entryRef, guildEntryToMap(entry, isCreate = true))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun importParsedTransactions(
        profile: UserProfile,
        items: List<com.example.domain.ParsedImportItem>
    ): Result<Int> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}/transactions"
        return try {
            val uid = requireUserId()
            val selected = items.filter { it.selectedForImport && !it.duplicateInLedger }
            if (selected.isEmpty()) return Result.success(0)

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            var checkingDelta = 0L
            var reserveDelta = 0L

            for (item in selected) {
                val txId = "ofx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                val txRef = userRef.collection("transactions").document(txId)
                val tx = TransactionRecord(
                    userId = uid,
                    description = item.description.trim().take(110).ifBlank { "Lançamento importado" },
                    amountCents = item.amountCents,
                    type = item.suggestedType.name,
                    paymentMethod = item.suggestedPaymentMethod.name,
                    categoryId = item.suggestedCategoryId,
                    tags = listOf("#Importado"),
                    dateEpochDay = item.epochDay,
                    competenceMonth = item.competenceMonth,
                    installmentIndex = 1,
                    installmentTotal = 1,
                    installmentGroupId = "",
                    dedupeHash = item.dedupeHash,
                    sharedGuild = false,
                    paidByUid = uid
                )
                batch.set(txRef, txToMap(tx, isCreate = true))

                when (item.suggestedType) {
                    TransactionType.EXPENSE -> checkingDelta -= item.amountCents
                    TransactionType.INCOME -> checkingDelta += item.amountCents
                    TransactionType.TRANSFERENCIA_INTERNA -> {
                        checkingDelta -= item.amountCents
                        reserveDelta += item.amountCents
                    }
                    else -> Unit
                }
            }

            batch.update(
                userRef,
                mapOf(
                    "checkingBalanceCents" to (profile.checkingBalanceCents + checkingDelta),
                    "emergencyReserveCents" to (profile.emergencyReserveCents + reserveDelta),
                    "hasSeenOfxTip" to true,
                    "xpPoints" to (profile.xpPoints + (selected.size * 10L)),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()
            Result.success(selected.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun reallocateBudget(
        profile: UserProfile,
        fromCategory: BudgetCategory,
        toCategory: BudgetCategory,
        shiftCents: Long
    ): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}/budgets"
        return try {
            val uid = requireUserId()
            val income = profile.monthlyNetIncomeCents.coerceAtLeast(1L)
            val deltaPct = (shiftCents.toDouble() / income.toDouble()) * 100.0
            val newFromPct = (fromCategory.percentageCap - deltaPct).coerceAtLeast(1.0)
            val actualDelta = fromCategory.percentageCap - newFromPct
            val newToPct = (toCategory.percentageCap + actualDelta).coerceAtMost(95.0)

            val batch = db.batch()
            val budgetsRef = db.collection("users").document(uid).collection("budgets")
            batch.update(
                budgetsRef.document(fromCategory.id),
                mapOf("percentageCap" to newFromPct, "updatedAt" to FieldValue.serverTimestamp())
            )
            batch.update(
                budgetsRef.document(toCategory.id),
                mapOf("percentageCap" to newToPct, "updatedAt" to FieldValue.serverTimestamp())
            )
            batch.update(
                db.collection("users").document(uid),
                mapOf("xpPoints" to (profile.xpPoints + 25L), "updatedAt" to FieldValue.serverTimestamp())
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateLanguageLevel(level: Int): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "languageLevel" to level.coerceIn(1, 3),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markTourSeen(seen: Boolean = true): Result<Unit> =
        updateBooleanProfileField("hasSeenTour", seen)

    suspend fun updateBooleanProfileField(field: String, value: Boolean): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid)
                .update(mapOf(field to value, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun unlockLesson(profile: UserProfile, lessonId: String, xpBonus: Int): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            if (lessonId in profile.unlockedLessons) return Result.success(Unit)
            val updated = (profile.unlockedLessons + lessonId).distinct().take(30)
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "unlockedLessons" to updated,
                        "xpPoints" to (profile.xpPoints + xpBonus),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePrivacyAndSplit(
        profile: UserProfile,
        guild: GuildVault?,
        newPrivacy: PrivacyMode,
        newSplitMode: GuildSplitMode
    ): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            val batch = db.batch()
            batch.update(
                db.collection("users").document(uid),
                mapOf(
                    "privacyMode" to newPrivacy.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (guild != null && guild.id.isNotBlank()) {
                val updatedPrivacyMap = guild.memberPrivacy.toMutableMap()
                updatedPrivacyMap[uid] = newPrivacy.name
                batch.update(
                    db.collection("guilds").document(guild.id),
                    mapOf(
                        "memberPrivacy" to updatedPrivacyMap,
                        "splitMode" to newSplitMode.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun recordGuildSettlement(
        profile: UserProfile,
        guild: GuildVault,
        debtorUid: String,
        debtorName: String,
        creditorName: String,
        amountCents: Long
    ): Result<Unit> {
        val path = "guilds/${guild.id}/entries"
        return try {
            val uid = requireUserId()
            require(amountCents > 0L) { "Nada para acertar neste mês." }
            val today = DateUtils.todayEpochDay()
            val month = DateUtils.currentYearMonth()

            val batch = db.batch()
            val entryId = "acerto_${UUID.randomUUID().toString().replace("-", "").take(14)}"
            val entry = GuildEntry(
                guildId = guild.id,
                memberUids = guild.memberUids,
                authorUid = uid,
                authorName = debtorName,
                description = "Acerto do Mês ($debtorName -> $creditorName)",
                amountCents = amountCents,
                categoryId = "cat_reserva",
                entryType = GuildEntryType.ACERTO_GUILDA.name,
                dateEpochDay = today,
                competenceMonth = month
            )
            batch.set(
                db.collection("guilds").document(guild.id).collection("entries").document(entryId),
                guildEntryToMap(entry, isCreate = true)
            )

            // Also record neutral ACERTO_GUILDA transaction in current user's ledger
            val txId = "tx_acerto_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val tx = TransactionRecord(
                userId = uid,
                description = "Acerto Guilda -> $creditorName",
                amountCents = amountCents,
                type = TransactionType.ACERTO_GUILDA.name,
                paymentMethod = PaymentMethod.DEBIT_PIX_CASH.name,
                categoryId = "cat_reserva",
                tags = listOf("#Guilda"),
                dateEpochDay = today,
                competenceMonth = month,
                installmentIndex = 1,
                installmentTotal = 1,
                installmentGroupId = "",
                dedupeHash = DemoDataFactory.buildDedupeHash(today, amountCents, "acerto_$entryId"),
                sharedGuild = true,
                paidByUid = debtorUid
            )
            batch.set(
                db.collection("users").document(uid).collection("transactions").document(txId),
                txToMap(tx, isCreate = true)
            )

            val checkingAdjust = if (debtorUid == uid) -amountCents else amountCents
            batch.update(
                db.collection("users").document(uid),
                mapOf(
                    "checkingBalanceCents" to (profile.checkingBalanceCents + checkingAdjust),
                    "xpPoints" to (profile.xpPoints + 35L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun joinGuildByInviteCode(
        profile: UserProfile,
        inviteCodeRaw: String
    ): Result<String> {
        val code = inviteCodeRaw.trim().uppercase()
        return try {
            val uid = requireUserId()
            require(code.length in 4..12 && code.matches(Regex("^[A-Za-z0-9_\\-]+$"))) {
                "Código de convite inválido."
            }
            val inviteSnap = db.collection("guildInvites").document(code).get().await()
            if (!inviteSnap.exists()) {
                return Result.failure(IllegalArgumentException("Convite '$code' não encontrado."))
            }
            val guildId = inviteSnap.getString("guildId")
                ?: return Result.failure(IllegalStateException("Convite sem Guilda associada."))

            val guildSnap = db.collection("guilds").document(guildId).get().await()
            if (!guildSnap.exists()) {
                return Result.failure(IllegalStateException("Guilda não encontrada."))
            }
            val guild = guildSnap.toObject(GuildVault::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return Result.failure(IllegalStateException("Erro ao ler Guilda."))

            if (uid in guild.memberUids) {
                return Result.success("Você já faz parte desta Guilda!")
            }
            if (guild.memberUids.size >= 2) {
                return Result.failure(IllegalStateException("Esta Guilda já está completa (2 heróis)."))
            }

            val newMembers = guild.memberUids + uid
            val newNames = guild.memberNames.toMutableMap().apply { put(uid, profile.heroName) }
            val newIncomes = guild.memberIncomesCents.toMutableMap().apply { put(uid, profile.monthlyNetIncomeCents) }
            val newPrivacy = guild.memberPrivacy.toMutableMap().apply { put(uid, profile.privacyMode) }

            val batch = db.batch()
            batch.update(
                db.collection("guilds").document(guildId),
                mapOf(
                    "memberUids" to newMembers,
                    "memberNames" to newNames,
                    "memberIncomesCents" to newIncomes,
                    "memberPrivacy" to newPrivacy,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(uid),
                mapOf("guildId" to guildId, "updatedAt" to FieldValue.serverTimestamp())
            )
            batch.commit().await()
            Result.success("Uniu-se à Guilda ${guild.guildName}!")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "guildInvites/$code")
            Result.failure(e)
        }
    }

    suspend fun saveBankAccount(account: BankAccount): Result<String> {
        val rawId = account.id.trim()
        val isCreate = rawId.isBlank()
        val targetId = if (isCreate) {
            "acc_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        } else {
            rawId
        }
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/accounts/$targetId"
        return try {
            val uid = requireUserId()
            require(account.name.trim().isNotBlank()) { "Nome da conta é obrigatório." }
            val docRef = db.collection("users").document(uid).collection("accounts").document(targetId)
            docPath = docRef.path
            val normalized = account.copy(userId = uid)
            if (isCreate) {
                docRef.set(normalized.toFirestoreMap(isCreate = true)).await()
            } else {
                docRef.update(normalized.toUpdateMap()).await()
            }
            Result.success(targetId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteBankAccount(accountId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/accounts/$accountId"
        return try {
            val uid = requireUserId()
            require(accountId.isNotBlank()) { "ID da conta inválido." }
            val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Serialization Maps (Exact 1:1 parity with firestore.rules, NO "id" field, filter null) ---

    private fun profileToMap(p: UserProfile, isCreate: Boolean): Map<String, Any> =
        p.toFirestoreMap(isCreate = isCreate)

    private fun budgetToMap(b: BudgetCategory, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to b.userId,
            "name" to b.name,
            "iconKey" to b.iconKey,
            "bucket" to b.bucket,
            "percentageCap" to b.percentageCap,
            "fixedCost" to b.fixedCost,
            "sortOrder" to b.sortOrder,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }

    private fun txToMap(t: TransactionRecord, isCreate: Boolean): Map<String, Any> =
        t.toFirestoreMap(isCreate = isCreate)

    private fun guildToMap(g: GuildVault, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "guildName" to g.guildName,
            "inviteCode" to g.inviteCode,
            "ownerId" to g.ownerId,
            "memberUids" to g.memberUids,
            "memberNames" to g.memberNames,
            "memberIncomesCents" to g.memberIncomesCents,
            "memberPrivacy" to g.memberPrivacy,
            "splitMode" to g.splitMode,
            "sharedGoalName" to g.sharedGoalName,
            "sharedGoalTargetCents" to g.sharedGoalTargetCents,
            "sharedGoalSavedCents" to g.sharedGoalSavedCents,
            "companionDemoName" to g.companionDemoName,
            "companionDemoIncomeCents" to g.companionDemoIncomeCents,
            "companionDemoPrivacy" to g.companionDemoPrivacy,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    private fun guildEntryToMap(e: GuildEntry, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "guildId" to e.guildId,
            "memberUids" to e.memberUids,
            "authorUid" to e.authorUid,
            "authorName" to e.authorName,
            "description" to e.description,
            "amountCents" to e.amountCents,
            "categoryId" to e.categoryId,
            "entryType" to e.entryType,
            "dateEpochDay" to e.dateEpochDay,
            "competenceMonth" to e.competenceMonth,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }
}
