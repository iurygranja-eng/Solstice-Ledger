package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BudgetCategory
import com.example.data.DateUtils
import com.example.data.FinancialClass
import com.example.data.GuildEntry
import com.example.data.GuildSplitMode
import com.example.data.GuildVault
import com.example.data.PaymentMethod
import com.example.data.PrivacyMode
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.example.data.ViewMode
import com.example.data.formatBrl
import com.example.data.repo.FinanceRepository
import com.example.data.repo.FinancialContextSnapshot
import com.example.domain.AccountingEngine
import com.example.domain.BreakEvenSnapshot
import com.example.domain.CategoryBudgetStatus
import com.example.domain.GuildSettlementSummary
import com.example.domain.MonthInstallmentSummary
import com.example.domain.ParsedImportItem
import com.example.domain.PredictiveStats
import com.example.domain.StatsEngine
import com.example.domain.TagCostSummary
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LedgerUiState {
    data object Loading : LedgerUiState
    data object NeedsHeroSetup : LedgerUiState
    data class Ready(
        val profile: UserProfile,
        val budgets: List<BudgetCategory>,
        val transactions: List<TransactionRecord>,
        val guild: GuildVault?,
        val guildEntries: List<GuildEntry>,
        val categoryStatuses: List<CategoryBudgetStatus>,
        val breakEven: BreakEvenSnapshot,
        val stats: PredictiveStats,
        val tagSummaries: List<TagCostSummary>,
        val futureInstallments: List<MonthInstallmentSummary>,
        val guildSettlement: GuildSettlementSummary?
    ) : LedgerUiState {
        fun toContextSnapshot(): FinancialContextSnapshot = FinancialContextSnapshot(
            profile = profile,
            categoryStatuses = categoryStatuses,
            breakEven = breakEven,
            stats = stats,
            guild = guild,
            guildSettlement = guildSettlement,
            transactions = transactions,
            tagSummaries = tagSummaries,
            futureInstallments = futureInstallments
        )
    }

    data class Error(val message: String) : LedgerUiState
}

data class JustInTimeTip(
    val title: String,
    val message: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModel(
    private val repository: FinanceRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _viewMode = MutableStateFlow(ViewMode.SOLO)
    val viewMode: StateFlow<com.example.data.ViewMode> = _viewMode.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _justInTimeTip = MutableStateFlow<JustInTimeTip?>(null)
    val justInTimeTip: StateFlow<JustInTimeTip?> = _justInTimeTip.asStateFlow()

    private val _replayTourRequested = MutableStateFlow(false)
    val replayTourRequested: StateFlow<Boolean> = _replayTourRequested.asStateFlow()

    private val _cycleStartDay = MutableStateFlow(1)
    val cycleStartDay: StateFlow<Int> = _cycleStartDay.asStateFlow()

    private val guildsFlow: Flow<List<GuildVault>> =
        repository.observeGuildsForUser(currentUserId)

    private val guildEntriesFlow: Flow<List<GuildEntry>> =
        guildsFlow.flatMapLatest { guilds ->
            val primary = guilds.firstOrNull()
            if (primary != null && primary.id.isNotBlank()) {
                repository.observeGuildEntries(primary.id, currentUserId)
            } else {
                flowOf(emptyList())
            }
        }

    private val coreInputsFlow = combine(
        repository.observeUserProfile(currentUserId),
        repository.observeBudgets(currentUserId),
        repository.observeTransactions(currentUserId),
        guildsFlow,
        guildEntriesFlow
    ) { profile, budgets, transactions, guilds, guildEntries ->
        RawLedgerInputs(profile, budgets, transactions, guilds, guildEntries)
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        coreInputsFlow,
        _cycleStartDay
    ) { raw, cycleStartDay ->
        val baseProfile = raw.profile
        if (baseProfile == null) {
            LedgerUiState.NeedsHeroSetup
        } else {
            val profile = baseProfile.copy(cycleStartDay = AccountingEngine.normalizeCycleStartDay(cycleStartDay))
            val budgets = raw.budgets
            val transactions = raw.transactions
            val guilds = raw.guilds
            val guildEntries = raw.guildEntries

            val activeWindow = AccountingEngine.getActiveCycleWindow(profile.cycleStartDay)
            val activeCycle = activeWindow.cycleId
            val catStatuses = AccountingEngine.computeCategoryStatuses(
                profile = profile,
                budgets = budgets,
                transactions = transactions,
                month = activeCycle,
                dayOfMonth = activeWindow.elapsedDays,
                daysInMonth = activeWindow.totalDays
            )
            val breakEven = AccountingEngine.computeBreakEven(
                profile = profile,
                budgets = budgets,
                transactions = transactions,
                month = activeCycle
            )
            val stats = StatsEngine.compute(
                profile = profile,
                budgets = budgets,
                transactions = transactions,
                categoryStatuses = catStatuses,
                month = activeCycle
            )
            val tags = AccountingEngine.aggregateByTag(transactions)
            val futureInst = AccountingEngine.groupFutureInstallments(
                transactions = transactions,
                currentMonth = activeCycle,
                cycleStartDay = profile.cycleStartDay
            )
            val primaryGuild = guilds.firstOrNull()
            val settlement = AccountingEngine.computeGuildSettlement(
                guild = primaryGuild,
                entries = guildEntries,
                currentUid = currentUserId,
                month = activeCycle
            )

            LedgerUiState.Ready(
                profile = profile,
                budgets = budgets,
                transactions = transactions,
                guild = primaryGuild,
                guildEntries = guildEntries,
                categoryStatuses = catStatuses,
                breakEven = breakEven,
                stats = stats,
                tagSummaries = tags,
                futureInstallments = futureInst,
                guildSettlement = settlement
            )
        }
    }
        .map<LedgerUiState, LedgerUiState> { it }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.e(TAG, "Error observing ledger state", rootError)
            emit(LedgerUiState.Error(rootError.message ?: "Erro ao carregar dados no Firestore."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = LedgerUiState.Loading
        )

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun dismissJustInTimeTip() {
        _justInTimeTip.value = null
    }

    fun setViewMode(mode: com.example.data.ViewMode) {
        _viewMode.value = mode
    }

    fun setCycleStartDay(day: Int) {
        _cycleStartDay.value = AccountingEngine.normalizeCycleStartDay(day)
    }

    fun requestReplayTour() {
        _replayTourRequested.value = true
    }

    fun finishTour() {
        _replayTourRequested.value = false
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        if (!ready.profile.hasSeenTour) {
            viewModelScope.launch {
                repository.markTourSeen(true)
            }
        }
    }

    fun createHero(
        heroName: String,
        avatarHair: Int,
        avatarOutfit: Int,
        avatarAura: String,
        financialClass: FinancialClass,
        monthlyNetIncomeCents: Long,
        includeDemoData: Boolean
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.createHeroAndSeedDemo(
                heroName = heroName,
                avatarHair = avatarHair,
                avatarOutfit = avatarOutfit,
                avatarAura = avatarAura,
                financialClass = financialClass,
                monthlyNetIncomeCents = monthlyNetIncomeCents,
                includeDemoData = includeDemoData
            )
            _isSubmitting.value = false
            result.onFailure {
                _statusMessage.value = "Não foi possível criar perfil: ${it.localizedMessage}"
            }
        }
    }

    fun setLanguageLevel(level: Int) {
        viewModelScope.launch {
            repository.updateLanguageLevel(level)
        }
    }

    fun addManualTransaction(
        description: String,
        amountCents: Long,
        type: TransactionType,
        paymentMethod: PaymentMethod,
        categoryId: String,
        tags: List<String>,
        installments: Int,
        sharedGuild: Boolean,
        onComplete: () -> Unit = {}
    ) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.addTransactionWithInstallments(
                profile = ready.profile,
                guild = ready.guild,
                description = description,
                amountCents = amountCents,
                type = type,
                paymentMethod = paymentMethod,
                categoryId = categoryId,
                tags = tags,
                installments = installments,
                sharedGuild = sharedGuild
            )
            _isSubmitting.value = false
            result.onSuccess {
                _statusMessage.value = when {
                    type == TransactionType.EXPENSE && paymentMethod == PaymentMethod.CREDIT_CARD && installments > 1 ->
                        "Compra parcelada em ${installments}x registrada! (+15 XP)"
                    AccountingEngine.isNeutralType(type.name) ->
                        "Movimentação neutra registrada sem distorcer tetos! (+15 XP)"
                    else -> "Lançamento salvo na Forja! (+15 XP)"
                }

                // Just-in-Time educational tips
                if (type == TransactionType.EXPENSE && paymentMethod == PaymentMethod.CREDIT_CARD && installments > 1 && !ready.profile.hasSeenInstallmentTip) {
                    _justInTimeTip.value = JustInTimeTip(
                        title = "Dica: Competência no Parcelamento",
                        message = "Cada parcela futura foi selada nos próximos ${installments - 1} meses para proteger seu orçamento, sem tirar dinheiro da conta hoje."
                    )
                } else if (AccountingEngine.isNeutralType(type.name) && !ready.profile.hasSeenTransferTip) {
                    _justInTimeTip.value = JustInTimeTip(
                        title = "Dica: Neutralidade Patrimonial",
                        message = "Transferências internas e pagamentos de fatura ajustam seu saldo em caixa sem contar como gasto duplicado nas categorias."
                    )
                }
                onComplete()
            }.onFailure {
                _statusMessage.value = it.localizedMessage ?: "Erro ao registrar lançamento."
            }
        }
    }

    fun payOpenCreditCardBill() {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        val bill = ready.profile.creditCardOpenBillCents
        if (bill <= 0L) {
            _statusMessage.value = "Sua fatura aberta já está quitada!"
            return
        }
        addManualTransaction(
            description = "Pagamento da Fatura Aberta",
            amountCents = bill,
            type = TransactionType.PAGAMENTO_FATURA,
            paymentMethod = PaymentMethod.DEBIT_PIX_CASH,
            categoryId = "cat_reserva",
            tags = listOf("#Fatura"),
            installments = 1,
            sharedGuild = false
        )
    }

    fun importParsedTransactions(items: List<ParsedImportItem>, onComplete: () -> Unit = {}) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.importParsedTransactions(ready.profile, items)
            _isSubmitting.value = false
            result.onSuccess { importedCount ->
                _statusMessage.value = if (importedCount > 0) {
                    "$importedCount lançamentos importados e deduplicados com sucesso!"
                } else {
                    "Nenhum item novo selecionado (duplicatas ignoradas)."
                }
                if (importedCount > 0 && !ready.profile.hasSeenOfxTip) {
                    _justInTimeTip.value = JustInTimeTip(
                        title = "Dica: Importação Inteligente",
                        message = "Transações idênticas foram ignoradas via hash único e transferências entre suas contas foram marcadas como neutras."
                    )
                }
                onComplete()
            }.onFailure {
                _statusMessage.value = it.localizedMessage ?: "Falha na importação."
            }
        }
    }

    fun reallocateBudgetByName(fromName: String, toName: String, shiftCents: Long) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        val fromCat = ready.budgets.firstOrNull { it.name.contains(fromName, ignoreCase = true) }
            ?: ready.budgets.firstOrNull { it.bucket == "LIFESTYLE" }
            ?: return
        val toCat = ready.budgets.firstOrNull { it.name.contains(toName, ignoreCase = true) }
            ?: ready.budgets.firstOrNull { it.id != fromCat.id }
            ?: return
        if (fromCat.id == toCat.id) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.reallocateBudget(ready.profile, fromCat, toCat, shiftCents)
            _isSubmitting.value = false
            result.onSuccess {
                _statusMessage.value = "Remanejados ${formatBrl(shiftCents)} de ${fromCat.name} para ${toCat.name}! (+25 XP)"
            }.onFailure {
                _statusMessage.value = it.localizedMessage ?: "Não foi possível remanejar."
            }
        }
    }

    fun unlockLesson(lessonId: String, xpBonus: Int) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        if (lessonId in ready.profile.unlockedLessons) return
        viewModelScope.launch {
            repository.unlockLesson(ready.profile, lessonId, xpBonus).onSuccess {
                _statusMessage.value = "Lição do Grimório desbloqueada! (+$xpBonus XP)"
            }
        }
    }

    fun updateGuildSettings(privacyMode: PrivacyMode, splitMode: GuildSplitMode) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        viewModelScope.launch {
            repository.updatePrivacyAndSplit(ready.profile, ready.guild, privacyMode, splitMode)
                .onSuccess {
                    _statusMessage.value = "Configurações da Guilda atualizadas."
                }
                .onFailure {
                    _statusMessage.value = it.localizedMessage ?: "Erro ao atualizar Guilda."
                }
        }
    }

    fun settleGuildMonth() {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        val guild = ready.guild ?: return
        val settlement = ready.guildSettlement ?: return
        if (settlement.settlementAmountCents <= 0L) {
            _statusMessage.value = "A Guilda já está 100% equilibrada este mês!"
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.recordGuildSettlement(
                profile = ready.profile,
                guild = guild,
                debtorUid = settlement.debtorUid,
                debtorName = settlement.debtorName,
                creditorName = settlement.creditorName,
                amountCents = settlement.settlementAmountCents
            ).onSuccess {
                _statusMessage.value = "Acerto de ${formatBrl(settlement.settlementAmountCents)} registrado como neutro! (+35 XP)"
            }.onFailure {
                _statusMessage.value = it.localizedMessage ?: "Falha ao registrar acerto."
            }
            _isSubmitting.value = false
        }
    }

    fun joinGuildByCode(code: String) {
        val ready = uiState.value as? LedgerUiState.Ready ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.joinGuildByInviteCode(ready.profile, code)
                .onSuccess { msg -> _statusMessage.value = msg }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Erro ao entrar na Guilda." }
            _isSubmitting.value = false
        }
    }

    private data class RawLedgerInputs(
        val profile: UserProfile?,
        val budgets: List<BudgetCategory>,
        val transactions: List<TransactionRecord>,
        val guilds: List<GuildVault>,
        val guildEntries: List<GuildEntry>
    )

    private companion object {
        const val TAG = "LedgerViewModel"
    }
}
