package com.example

import androidx.annotation.Keep
import com.example.data.BankAccount
import com.example.data.BankAccountType
import com.example.data.FinancialClass
import com.example.data.PaymentMethod
import com.example.data.PrivacyMode
import com.example.data.TransactionRecord
import com.example.data.TransactionType
import com.example.data.UserProfile
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreModelsTest {

    @Test
    fun `UserProfile BankAccount and TransactionRecord expose no-arg constructors and Firestore annotations`() {
        val modelClasses = listOf(
            UserProfile::class.java,
            BankAccount::class.java,
            TransactionRecord::class.java
        )

        for (clazz in modelClasses) {
            assertTrue(
                "${clazz.simpleName} must have @IgnoreExtraProperties",
                clazz.isAnnotationPresent(IgnoreExtraProperties::class.java)
            )

            val instance = clazz.getDeclaredConstructor().newInstance()
            assertNotNull("${clazz.simpleName} must support no-arg instantiation", instance)

            val idField = clazz.getDeclaredField("id")
            assertTrue("${clazz.simpleName}.id must carry @DocumentId", idField.isAnnotationPresent(DocumentId::class.java))

            val createdAtField = clazz.getDeclaredField("createdAt")
            val updatedAtField = clazz.getDeclaredField("updatedAt")
            assertTrue(
                "${clazz.simpleName}.createdAt must carry @ServerTimestamp",
                createdAtField.isAnnotationPresent(ServerTimestamp::class.java)
            )
            assertTrue(
                "${clazz.simpleName}.updatedAt must carry @ServerTimestamp",
                updatedAtField.isAnnotationPresent(ServerTimestamp::class.java)
            )

            // Guard against Kotlin Boolean 'is'-prefix CustomClassMapper serialization mismatch
            val isPrefixedBooleans = clazz.declaredFields.filter { field ->
                (field.type == Boolean::class.javaPrimitiveType || field.type == Boolean::class.javaObjectType) &&
                    field.name.startsWith("is")
            }
            assertTrue(
                "${clazz.simpleName} must not have 'is'-prefixed Boolean fields: $isPrefixedBooleans",
                isPrefixedBooleans.isEmpty()
            )
        }
    }

    @Test
    fun `UserProfile toFirestoreMap omits id and null guildId while applying server timestamps`() {
        val profile = UserProfile(
            id = "uid_should_not_serialize",
            userId = "uid_123",
            heroName = "Aeloria",
            financialClass = FinancialClass.EXPLORER.name,
            cycleStartDay = 5,
            guildId = null
        )

        val createMap = profile.toFirestoreMap(isCreate = true)
        assertFalse(createMap.containsKey("id"))
        assertFalse(createMap.containsKey("guildId"))
        assertEquals("uid_123", createMap["userId"])
        assertEquals(5, createMap["cycleStartDay"])
        assertTrue(createMap["createdAt"] is FieldValue)
        assertTrue(createMap["updatedAt"] is FieldValue)

        val updateMap = profile.copy(guildId = "guild_sol_1").toFirestoreMap(isCreate = false)
        assertFalse(updateMap.containsKey("id"))
        assertFalse(updateMap.containsKey("createdAt"))
        assertEquals("guild_sol_1", updateMap["guildId"])
        assertTrue(updateMap["updatedAt"] is FieldValue)
    }

    @Test
    fun `BankAccount toFirestoreMap and toUpdateMap preserve Long centavos and omit id`() {
        val account = BankAccount(
            id = "acc_1",
            userId = "uid_123",
            name = "Bolsa Lunar",
            institution = "Banco Sol",
            accountType = BankAccountType.SAVINGS.name,
            balanceCents = 1_250_000L,
            primaryAccount = true,
            includeInSafeToSpend = false,
            archived = false,
            sortOrder = 2
        )

        val createMap = account.toFirestoreMap(isCreate = true)
        assertFalse(createMap.containsKey("id"))
        assertEquals("uid_123", createMap["userId"])
        assertEquals(1_250_000L, createMap["balanceCents"])
        assertEquals("SAVINGS", createMap["accountType"])
        assertEquals(true, createMap["primaryAccount"])
        assertEquals(false, createMap["includeInSafeToSpend"])
        assertTrue(createMap["createdAt"] is FieldValue)
        assertTrue(createMap["updatedAt"] is FieldValue)

        val updateMap = account.toUpdateMap()
        assertFalse(updateMap.containsKey("id"))
        assertFalse(updateMap.containsKey("userId"))
        assertFalse(updateMap.containsKey("createdAt"))
        assertTrue(updateMap["updatedAt"] is FieldValue)
    }

    @Test
    fun `TransactionRecord toFirestoreMap strips null optional foreign keys and keeps valid ones`() {
        val withoutLinks = TransactionRecord(
            id = "tx_1",
            userId = "uid_123",
            description = "Padaria Estrela",
            amountCents = 3_450L,
            accountId = null,
            cardId = "   "
        )
        val mapWithoutLinks = withoutLinks.toFirestoreMap(isCreate = true)
        assertFalse(mapWithoutLinks.containsKey("id"))
        assertFalse(mapWithoutLinks.containsKey("accountId"))
        assertFalse(mapWithoutLinks.containsKey("cardId"))
        assertEquals("uid_123", mapWithoutLinks["paidByUid"])
        assertTrue(mapWithoutLinks["createdAt"] is FieldValue)

        val withLinks = withoutLinks.copy(accountId = "acc_1", cardId = "card_lunar")
        val mapWithLinks = withLinks.toFirestoreMap(isCreate = false)
        assertEquals("acc_1", mapWithLinks["accountId"])
        assertEquals("card_lunar", mapWithLinks["cardId"])
        assertNull(mapWithLinks["createdAt"])
    }

    @Test
    fun `Enum helper accessors fall back safely on unknown Firestore strings`() {
        val profile = UserProfile(financialClass = "UNKNOWN_CLASS", privacyMode = "LEGACY_MODE")
        assertEquals(FinancialClass.GUARDIAN, profile.financialClassEnum())
        assertEquals(PrivacyMode.CATEGORY_SUMMARY, profile.privacyModeEnum())

        val account = BankAccount(accountType = "CRYPTO_VAULT")
        assertEquals(BankAccountType.CHECKING, account.accountTypeEnum())

        val tx = TransactionRecord(type = "FUTURE_TYPE", paymentMethod = "FUTURE_METHOD")
        assertEquals(TransactionType.EXPENSE, tx.typeEnum())
        assertEquals(PaymentMethod.DEBIT_PIX_CASH, tx.paymentMethodEnum())
    }
}
