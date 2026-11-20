package com.example.domain

import com.example.data.DateUtils
import com.example.data.DemoDataFactory
import com.example.data.PaymentMethod
import com.example.data.TransactionType
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

data class ParsedImportItem(
    val description: String,
    val amountCents: Long,
    val epochDay: Long,
    val competenceMonth: String,
    val suggestedCategoryId: String,
    val suggestedType: TransactionType,
    val suggestedPaymentMethod: PaymentMethod,
    val dedupeHash: String,
    val duplicateInLedger: Boolean,
    val selectedForImport: Boolean
)

object OfxCsvParser {

    fun parse(
        rawContent: String,
        fileName: String,
        existingHashes: Set<String>
    ): List<ParsedImportItem> {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) return emptyList()

        val rawItems = if (
            fileName.endsWith(".ofx", ignoreCase = true) ||
            trimmed.contains("<STMTTRN>", ignoreCase = true) ||
            trimmed.contains("<OFX>", ignoreCase = true)
        ) {
            parseOfx(trimmed)
        } else {
            parseCsv(trimmed)
        }

        val seenInBatch = mutableSetOf<String>()
        return rawItems.map { item ->
            val isDup = item.dedupeHash in existingHashes || !seenInBatch.add(item.dedupeHash)
            item.copy(
                duplicateInLedger = isDup,
                selectedForImport = !isDup
            )
        }
    }

    private fun parseOfx(content: String): List<ParsedImportItem> {
        val blocks = Regex("<STMTTRN>(.*?)</STMTTRN>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(content)
            .map { it.groupValues[1] }
            .toList()

        val fallbackBlocks = if (blocks.isEmpty()) {
            content.split(Regex("<STMTTRN>", RegexOption.IGNORE_CASE)).drop(1)
        } else {
            blocks
        }

        return fallbackBlocks.mapNotNull { block ->
            val dtRaw = extractOfxTag(block, "DTPOSTED")
            val amtRaw = extractOfxTag(block, "TRNAMT")
            val memo = extractOfxTag(block, "MEMO").ifBlank { extractOfxTag(block, "NAME") }
            if (amtRaw.isBlank() || memo.isBlank()) return@mapNotNull null

            val signedCents = parseAmountCents(amtRaw) ?: return@mapNotNull null
            if (signedCents == 0L) return@mapNotNull null

            val epochDay = parseOfxDate(dtRaw)
            buildParsedItem(memo, signedCents, epochDay)
        }
    }

    private fun extractOfxTag(block: String, tag: String): String {
        val regex = Regex("<$tag>([^<\\r\\n]+)", RegexOption.IGNORE_CASE)
        return regex.find(block)?.groupValues?.getOrNull(1)?.trim() ?: ""
    }

    private fun parseCsv(content: String): List<ParsedImportItem> {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val separator = if (lines.first().count { it == ';' } >= lines.first().count { it == ',' }) ';' else ','
        val dataLines = if (lines.first().lowercase(Locale.ROOT).let {
                it.contains("data") || it.contains("date") || it.contains("valor") || it.contains("amount")
            }
        ) {
            lines.drop(1)
        } else {
            lines
        }

        return dataLines.mapNotNull { line ->
            val cols = line.split(separator).map { it.trim().trim('"') }
            if (cols.size < 3) return@mapNotNull null

            val dateStr = cols[0]
            val desc = cols[1].ifBlank { "Lançamento Importado" }
            val amtRaw = cols[2]
            val signedCents = parseAmountCents(amtRaw) ?: return@mapNotNull null
            if (signedCents == 0L) return@mapNotNull null

            val epochDay = parseFlexibleDate(dateStr)
            buildParsedItem(desc, signedCents, epochDay)
        }
    }

    private fun buildParsedItem(
        description: String,
        signedCents: Long,
        epochDay: Long
    ): ParsedImportItem {
        val cleanDesc = description.trim().take(110)
        val absCents = abs(signedCents)
        val lower = cleanDesc.lowercase(Locale.ROOT)

        val isInternalTransfer = lower.contains("mesma titularidade") ||
            lower.contains("transf propria") ||
            lower.contains("resgate") ||
            lower.contains("aplicacao") ||
            lower.contains("aplicação")

        val isBillPayment = lower.contains("pagamento fatura") ||
            lower.contains("pagto cartao") ||
            lower.contains("fatura cartao")

        val suggestedType = when {
            isBillPayment -> TransactionType.PAGAMENTO_FATURA
            isInternalTransfer -> TransactionType.TRANSFERENCIA_INTERNA
            signedCents > 0 -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        val suggestedCategory = suggestCategoryId(lower, suggestedType)
        val dedupeHash = DemoDataFactory.buildDedupeHash(epochDay, absCents, cleanDesc)

        return ParsedImportItem(
            description = cleanDesc,
            amountCents = absCents,
            epochDay = epochDay,
            competenceMonth = DateUtils.epochDayToYearMonth(epochDay),
            suggestedCategoryId = suggestedCategory,
            suggestedType = suggestedType,
            suggestedPaymentMethod = PaymentMethod.DEBIT_PIX_CASH,
            dedupeHash = dedupeHash,
            duplicateInLedger = false,
            selectedForImport = true
        )
    }

    private fun suggestCategoryId(lowerDesc: String, type: TransactionType): String {
        if (type == TransactionType.TRANSFERENCIA_INTERNA || type == TransactionType.PAGAMENTO_FATURA) {
            return "cat_reserva"
        }
        return when {
            listOf("mercado", "supermercado", "ifood", "padaria", "restaurante", "cafe", "café", "lanche")
                .any { lowerDesc.contains(it) } -> "cat_alimentacao"
            listOf("aluguel", "condominio", "condomínio", "energia", "luz", "agua", "água", "internet", "fibra")
                .any { lowerDesc.contains(it) } -> "cat_moradia"
            listOf("farmacia", "farmácia", "uber", "99", "posto", "combustivel", "pet", "vet", "consulta")
                .any { lowerDesc.contains(it) } -> "cat_saude_pet"
            listOf("cinema", "viagem", "hotel", "steam", "spotify", "netflix", "show", "lazer")
                .any { lowerDesc.contains(it) } -> "cat_lazer"
            else -> "cat_alimentacao"
        }
    }

    private fun parseAmountCents(raw: String): Long? {
        val cleaned = raw.replace("R$", "")
            .replace(" ", "")
            .trim()
        if (cleaned.isEmpty()) return null

        val normalized = if (cleaned.contains(",") && cleaned.contains(".")) {
            // e.g. 1.234,56
            if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                cleaned.replace(".", "").replace(",", ".")
            } else {
                cleaned.replace(",", "")
            }
        } else if (cleaned.contains(",")) {
            cleaned.replace(",", ".")
        } else {
            cleaned
        }

        return normalized.toDoubleOrNull()?.let { (it * 100.0).roundToLong() }
    }

    private fun parseOfxDate(dtRaw: String): Long {
        val digits = dtRaw.filter { it.isDigit() }
        if (digits.length >= 8) {
            val year = digits.substring(0, 4).toIntOrNull() ?: 2026
            val month = digits.substring(4, 6).toIntOrNull() ?: 11
            val day = digits.substring(6, 8).toIntOrNull() ?: 1
            return DateUtils.parseToEpochDay(year, month, day)
        }
        return DateUtils.todayEpochDay()
    }

    private fun parseFlexibleDate(dateRaw: String): Long {
        val slashParts = dateRaw.split("/")
        if (slashParts.size == 3) {
            val d = slashParts[0].toIntOrNull() ?: 1
            val m = slashParts[1].toIntOrNull() ?: 11
            val y = slashParts[2].take(4).toIntOrNull() ?: 2026
            return DateUtils.parseToEpochDay(y, m, d)
        }
        val dashParts = dateRaw.split("-")
        if (dashParts.size == 3) {
            val y = dashParts[0].toIntOrNull() ?: 2026
            val m = dashParts[1].toIntOrNull() ?: 11
            val d = dashParts[2].take(2).toIntOrNull() ?: 1
            return DateUtils.parseToEpochDay(y, m, d)
        }
        return DateUtils.todayEpochDay()
    }

    fun sampleDemoOfx(): String {
        val ym = DateUtils.currentYearMonth().replace("-", "")
        return """
            <OFX>
              <STMTTRN>
                <TRNTYPE>DEBIT</TRNTYPE>
                <DTPOSTED>${ym}12120000</DTPOSTED>
                <TRNAMT>-128.90</TRNAMT>
                <MEMO>Supermercado Estrela Express</MEMO>
              </STMTTRN>
              <STMTTRN>
                <TRNTYPE>DEBIT</TRNTYPE>
                <DTPOSTED>${ym}13140000</DTPOSTED>
                <TRNAMT>-64.50</TRNAMT>
                <MEMO>Uber Viagem Noturna</MEMO>
              </STMTTRN>
              <STMTTRN>
                <TRNTYPE>XFER</TRNTYPE>
                <DTPOSTED>${ym}14090000</DTPOSTED>
                <TRNAMT>-350.00</TRNAMT>
                <MEMO>PIX Mesma Titularidade Reserva</MEMO>
              </STMTTRN>
            </OFX>
        """.trimIndent()
    }
}
