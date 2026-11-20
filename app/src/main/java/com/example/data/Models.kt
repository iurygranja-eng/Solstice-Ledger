package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

inline fun <reified T : Enum<T>> safeEnumOf(raw: String?, default: T): T {
    if (raw.isNullOrBlank()) return default
    return enumValues<T>().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: default
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.toFirestorePayload(): Map<String, Any> =
    filterValues { it != null } as Map<String, Any>

enum class FinancialClass(
    val titlePt: String,
    val subtitlePt: String,
    val essentialPct: Int,
    val lifestylePct: Int,
    val reservePct: Int
) {
    GUARDIAN(
        titlePt = "Guardião",
        subtitlePt = "Segurança • 50% Essenciais / 20% Estilo / 30% Reserva",
        essentialPct = 50,
        lifestylePct = 20,
        reservePct = 30
    ),
    EXPLORER(
        titlePt = "Explorador",
        subtitlePt = "Equilíbrio • 55% Essenciais / 30% Estilo / 15% Reserva",
        essentialPct = 55,
        lifestylePct = 30,
        reservePct = 15
    ),
    ALCHEMIST(
        titlePt = "Alquimista",
        subtitlePt = "Reestruturação • 60% Essenciais / 15% Estilo / 25% Quitação",
        essentialPct = 60,
        lifestylePct = 15,
        reservePct = 25
    )
}

enum class BudgetBucket {
    ESSENTIAL,
    LIFESTYLE,
    RESERVE
}

enum class BankAccountType(val labelPt: String) {
    CHECKING("Conta Corrente"),
    SAVINGS("Poupança / Reserva"),
    CASH("Carteira / Dinheiro"),
    INVESTMENT("Investimento Líquido")
}

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFERENCIA_INTERNA,
    PAGAMENTO_FATURA,
    ACERTO_GUILDA
}

enum class PaymentMethod {
    DEBIT_PIX_CASH,
    CREDIT_CARD
}

enum class PrivacyMode(val labelPt: String, val descPt: String) {
    TRANSPARENT("Transparente", "Parceiro vê lançamentos individuais e totais"),
    CATEGORY_SUMMARY("Resumo por Categoria", "Parceiro vê apenas totais por categoria"),
    PRIVATE("Privado", "Contas solo totalmente ocultas da Guilda")
}

enum class GuildSplitMode(val labelPt: String) {
    FIFTY_FIFTY("Divisão 50/50"),
    PROPORTIONAL("Proporcional à Renda")
}

enum class GuildEntryType {
    SHARED_EXPENSE,
    SHARED_GOAL_DEPOSIT,
    ACERTO_GUILDA
}

enum class ViewMode {
    SOLO,
    GUILDA
}

/**
 * Firestore document at `/users/{userId}`.
 * Stores all monetary balances in BRL centavos (`Long`) and uses safe defaults + `@DocumentId`
 * + `@ServerTimestamp` for crash-safe Firestore serialization and deserialization.
 */
@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val heroName: String = "Viajante",
    val avatarHair: Int = 0,
    val avatarOutfit: Int = 0,
    val avatarAura: String = "CYAN",
    val financialClass: String = FinancialClass.GUARDIAN.name,
    val monthlyNetIncomeCents: Long = 680_000L,
    val checkingBalanceCents: Long = 342_000L,
    val creditCardOpenBillCents: Long = 138_500L,
    val emergencyReserveCents: Long = 1_450_000L,
    val savingsGoalCents: Long = 136_000L,
    val cycleStartDay: Int = 1,
    val languageLevel: Int = 2,
    val privacyMode: String = PrivacyMode.CATEGORY_SUMMARY.name,
    val guildId: String? = null,
    val xpPoints: Long = 240L,
    val hasSeenTour: Boolean = false,
    val hasSeenInstallmentTip: Boolean = false,
    val hasSeenTransferTip: Boolean = false,
    val hasSeenOfxTip: Boolean = false,
    val unlockedLessons: List<String> = listOf("lesson_competencia"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @Exclude
    fun financialClassEnum(): FinancialClass = safeEnumOf(financialClass, FinancialClass.GUARDIAN)

    @Exclude
    fun privacyModeEnum(): PrivacyMode = safeEnumOf(privacyMode, PrivacyMode.CATEGORY_SUMMARY)

    @Exclude
    fun toFirestoreMap(isCreate: Boolean = true): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "heroName" to heroName.trim().ifBlank { "Viajante" }.take(40),
            "avatarHair" to avatarHair.coerceIn(0, 10),
            "avatarOutfit" to avatarOutfit.coerceIn(0, 10),
            "avatarAura" to avatarAura,
            "financialClass" to financialClassEnum().name,
            "monthlyNetIncomeCents" to monthlyNetIncomeCents.coerceIn(0L, 100_000_000_00L),
            "checkingBalanceCents" to checkingBalanceCents.coerceIn(-100_000_000_00L, 100_000_000_00L),
            "creditCardOpenBillCents" to creditCardOpenBillCents.coerceIn(0L, 100_000_000_00L),
            "emergencyReserveCents" to emergencyReserveCents.coerceIn(0L, 100_000_000_00L),
            "savingsGoalCents" to savingsGoalCents.coerceIn(0L, 100_000_000_00L),
            "cycleStartDay" to cycleStartDay.coerceIn(1, 28),
            "languageLevel" to languageLevel.coerceIn(1, 3),
            "privacyMode" to privacyModeEnum().name,
            "guildId" to guildId?.takeIf { it.isNotBlank() }?.take(128),
            "xpPoints" to xpPoints.coerceAtLeast(0L),
            "hasSeenTour" to hasSeenTour,
            "hasSeenInstallmentTip" to hasSeenInstallmentTip,
            "hasSeenTransferTip" to hasSeenTransferTip,
            "hasSeenOfxTip" to hasSeenOfxTip,
            "unlockedLessons" to unlockedLessons.distinct().take(30),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map.toFirestorePayload()
    }
}

/**
 * Firestore document at `/users/{userId}/accounts/{accountId}` ("Bolsas de Ouro").
 * Represents a user's checking, savings/reserve, cash, or liquid investment account in BRL centavos.
 */
@Keep
@IgnoreExtraProperties
data class BankAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Bolsa Principal",
    val institution: String = "",
    val accountType: String = BankAccountType.CHECKING.name,
    val balanceCents: Long = 0L,
    val colorHex: String = "#5EEAD4",
    val iconKey: String = "WALLET",
    val primaryAccount: Boolean = false,
    val includeInSafeToSpend: Boolean = true,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @Exclude
    fun accountTypeEnum(): BankAccountType = safeEnumOf(accountType, BankAccountType.CHECKING)

    @Exclude
    fun toFirestoreMap(isCreate: Boolean = true): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "name" to name.trim().ifBlank { "Bolsa Principal" }.take(40),
            "institution" to institution.trim().take(40),
            "accountType" to accountTypeEnum().name,
            "balanceCents" to balanceCents.coerceIn(-100_000_000_00L, 100_000_000_00L),
            "colorHex" to colorHex.trim().ifBlank { "#5EEAD4" }.take(16),
            "iconKey" to iconKey.trim().ifBlank { "WALLET" }.take(30),
            "primaryAccount" to primaryAccount,
            "includeInSafeToSpend" to includeInSafeToSpend,
            "archived" to archived,
            "sortOrder" to sortOrder.coerceIn(0, 50),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map.toFirestorePayload()
    }

    @Exclude
    fun toUpdateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "name" to name.trim().ifBlank { "Bolsa Principal" }.take(40),
            "institution" to institution.trim().take(40),
            "accountType" to accountTypeEnum().name,
            "balanceCents" to balanceCents.coerceIn(-100_000_000_00L, 100_000_000_00L),
            "colorHex" to colorHex.trim().ifBlank { "#5EEAD4" }.take(16),
            "iconKey" to iconKey.trim().ifBlank { "WALLET" }.take(30),
            "primaryAccount" to primaryAccount,
            "includeInSafeToSpend" to includeInSafeToSpend,
            "archived" to archived,
            "sortOrder" to sortOrder.coerceIn(0, 50),
            "updatedAt" to FieldValue.serverTimestamp()
        ).toFirestorePayload()
    }
}

@Keep
@IgnoreExtraProperties
data class CreditCard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Cristal Principal",
    val limitCents: Long = 500_000L,
    val closingDay: Int = 20,
    val dueDay: Int = 27
)

@Keep
@IgnoreExtraProperties
data class BudgetCategory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val iconKey: String = "STAR",
    val bucket: String = BudgetBucket.ESSENTIAL.name,
    val percentageCap: Double = 20.0,
    val fixedCost: Boolean = false,
    val allowRollover: Boolean = false,
    val rolloverCarryCents: Long = 0L,
    val sortOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * Firestore document at `/users/{userId}/transactions/{txId}`.
 * Tracks competence vs cash flow, credit card installments, optional linked `accountId` / `cardId`,
 * cross-cutting tags, and OFX/CSV deduplication hashes.
 */
@Keep
@IgnoreExtraProperties
data class TransactionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val description: String = "",
    val amountCents: Long = 0L,
    val type: String = TransactionType.EXPENSE.name,
    val paymentMethod: String = PaymentMethod.DEBIT_PIX_CASH.name,
    val categoryId: String = "cat_food",
    val accountId: String? = null,
    val cardId: String? = null,
    val tags: List<String> = emptyList(),
    val dateEpochDay: Long = DateUtils.todayEpochDay(),
    val competenceMonth: String = DateUtils.currentYearMonth(),
    val installmentIndex: Int = 1,
    val installmentTotal: Int = 1,
    val installmentGroupId: String = "",
    val dedupeHash: String = "",
    val sharedGuild: Boolean = false,
    val paidByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @Exclude
    fun typeEnum(): TransactionType = safeEnumOf(type, TransactionType.EXPENSE)

    @Exclude
    fun paymentMethodEnum(): PaymentMethod = safeEnumOf(paymentMethod, PaymentMethod.DEBIT_PIX_CASH)

    @Exclude
    fun toFirestoreMap(isCreate: Boolean = true): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "description" to description.trim().ifBlank { "Lançamento" }.take(120),
            "amountCents" to amountCents.coerceIn(1L, 100_000_000_00L),
            "type" to typeEnum().name,
            "paymentMethod" to paymentMethodEnum().name,
            "categoryId" to categoryId.trim().ifBlank { "cat_food" }.take(64),
            "accountId" to accountId?.trim()?.takeIf { it.isNotBlank() }?.take(64),
            "cardId" to cardId?.trim()?.takeIf { it.isNotBlank() }?.take(64),
            "tags" to tags.map { it.trim().take(24) }.filter { it.isNotBlank() }.take(10),
            "dateEpochDay" to dateEpochDay.coerceAtLeast(0L),
            "competenceMonth" to competenceMonth.trim().take(7),
            "installmentIndex" to installmentIndex.coerceIn(1, 60),
            "installmentTotal" to installmentTotal.coerceIn(1, 60),
            "installmentGroupId" to installmentGroupId.trim().take(64),
            "dedupeHash" to dedupeHash.trim().take(128),
            "sharedGuild" to sharedGuild,
            "paidByUid" to paidByUid.ifBlank { userId }.take(128),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map.toFirestorePayload()
    }
}

@Keep
@IgnoreExtraProperties
data class GuildVault(
    @DocumentId val id: String = "",
    val guildName: String = "Casa Sol & Lua",
    val inviteCode: String = "SOL202",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap(),
    val memberIncomesCents: Map<String, Long> = emptyMap(),
    val memberPrivacy: Map<String, String> = emptyMap(),
    val splitMode: String = GuildSplitMode.PROPORTIONAL.name,
    val sharedGoalName: String = "Reserva Lunar & Viagem",
    val sharedGoalTargetCents: Long = 2_500_000L,
    val sharedGoalSavedCents: Long = 980_000L,
    val companionDemoName: String? = null,
    val companionDemoIncomeCents: Long? = null,
    val companionDemoPrivacy: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GuildEntry(
    @DocumentId val id: String = "",
    val guildId: String = "",
    val memberUids: List<String> = emptyList(),
    val authorUid: String = "",
    val authorName: String = "",
    val description: String = "",
    val amountCents: Long = 0L,
    val categoryId: String = "cat_housing",
    val entryType: String = GuildEntryType.SHARED_EXPENSE.name,
    val dateEpochDay: Long = DateUtils.todayEpochDay(),
    val competenceMonth: String = DateUtils.currentYearMonth(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// --- Formatting & Date Utilities (API 24+ safe) ---

private val brLocale = Locale("pt", "BR")

fun formatBrl(cents: Long, includeSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(brLocale)
    val absFormatted = nf.format(abs(cents) / 100.0)
    return when {
        cents < 0 -> "-$absFormatted"
        includeSign && cents > 0 -> "+$absFormatted"
        else -> absFormatted
    }
}

object DateUtils {
    private const val MILLIS_PER_DAY = 86_400_000L
    private val utcTz = java.util.TimeZone.getTimeZone("UTC")

    fun todayEpochDay(): Long {
        val localCal = Calendar.getInstance()
        return parseToEpochDay(
            year = localCal.get(Calendar.YEAR),
            month1to12 = localCal.get(Calendar.MONTH) + 1,
            day = localCal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentYearMonth(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun currentDayOfMonth(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun daysInCurrentMonth(): Int =
        Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    fun remainingDaysInCurrentMonth(): Int {
        val total = daysInCurrentMonth()
        val current = currentDayOfMonth()
        return (total - current + 1).coerceAtLeast(1)
    }

    fun addMonths(yearMonth: String, offset: Int): String {
        val (year, month1to12) = parseYearMonth(yearMonth)
        val cal = Calendar.getInstance(utcTz)
        cal.clear()
        cal.set(year, month1to12 - 1, 1, 12, 0, 0)
        cal.add(Calendar.MONTH, offset)
        return String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
    }

    fun parseYearMonth(yearMonth: String): Pair<Int, Int> {
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month1to12 = (parts.getOrNull(1)?.toIntOrNull() ?: 1).coerceIn(1, 12)
        return year to month1to12
    }

    fun daysInMonth(year: Int, month1to12: Int): Int {
        val cal = Calendar.getInstance(utcTz)
        cal.clear()
        cal.set(year, (month1to12 - 1).coerceIn(0, 11), 1, 12, 0, 0)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun formatYearMonthShort(yearMonth: String): String {
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.takeLast(2) ?: "26"
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val names = listOf(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
        )
        val label = names.getOrElse((month - 1).coerceIn(0, 11)) { "Mes" }
        return "$label/$year"
    }

    fun epochDayToYearMonthDay(epochDay: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance(utcTz)
        cal.timeInMillis = epochDay * MILLIS_PER_DAY + (MILLIS_PER_DAY / 2L)
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun epochDayToDisplay(epochDay: Long): String {
        val (_, m, d) = epochDayToYearMonthDay(epochDay)
        return String.format(Locale.US, "%02d/%02d", d, m)
    }

    fun epochDayToYearMonth(epochDay: Long): String {
        val (y, m, _) = epochDayToYearMonthDay(epochDay)
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun parseToEpochDay(year: Int, month1to12: Int, day: Int): Long {
        val maxDay = daysInMonth(year, month1to12)
        val cal = Calendar.getInstance(utcTz)
        cal.clear()
        cal.set(year, (month1to12 - 1).coerceIn(0, 11), day.coerceIn(1, maxDay), 12, 0, 0)
        return cal.timeInMillis / MILLIS_PER_DAY
    }
}
