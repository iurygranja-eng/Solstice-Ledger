package com.example.domain

import com.example.data.UserProfile

enum class AvatarWisdomStage(
    val minLevel: Int,
    val titlePt: String,
    val auraDescPt: String
) {
    APRENDIZ(1, "Aprendiz da Taverna", "Moldura índigo simples"),
    ESCRIBA(2, "Escriba Lunar", "Borda dupla ciano & cristal"),
    GUARDIAO(3, "Guardião Astral", "Estrelas cardeais & halo"),
    ESTRATEGA(4, "Estrategista Arcano", "Selo rúnico pulsante"),
    ARQUIMAGO(5, "Arquimago do Solstício", "Aura celestial dupla & luz solar");

    companion object {
        fun forLevel(level: Int): AvatarWisdomStage = when {
            level >= 5 -> ARQUIMAGO
            level == 4 -> ESTRATEGA
            level == 3 -> GUARDIAO
            level == 2 -> ESCRIBA
            else -> APRENDIZ
        }
    }
}

enum class AvatarCostumeTier(
    val requiredNetWorthCents: Long,
    val titlePt: String,
    val lorePt: String
) {
    TRAVELER_TUNIC(0L, "Túnica de Viajante", "Veste inicial de tecido índigo leve."),
    LUNAR_MANTLE(250_000L, "Manto do Aprendiz Lunar", "Ombreiras prateadas e gola ciano."),
    MITHRIL_BRIGANDINE(1_000_000L, "Brigandina de Mithril", "Armadura escamada com fecho dourado."),
    SOLSTICE_COAT(2_500_000L, "Cota Real do Solstício", "Capa heráldica magenta com brasão solar."),
    CELESTIAL_ARMOR(5_000_000L, "Armadura Celestial 32-Bit", "Placas estelares douradas e runas vivas.")
}

enum class AvatarAccessoryTier(
    val requiredNetWorthCents: Long,
    val titlePt: String,
    val lorePt: String
) {
    COPPER_PIN(0L, "Broche de Cobre", "Insígnia simples de quem iniciou a jornada."),
    CRYSTAL_AMULET(100_000L, "Amuleto Cristalino", "Gema lunar que brilha ao equilibrar o caixa."),
    CLARITY_DIADEM(750_000L, "Diadema da Clareza", "Tiara de prata élfica forjada pela disciplina."),
    MANA_ORBITAL(2_000_000L, "Orbe de Mana Flutuante", "Esfera arcana que orbita o retrato do herói."),
    SOLAR_CROWN(4_500_000L, "Coroa do Solstício", "Coroa dourada de 3 pontas com rubi solar.")
}

enum class AvatarMascotTier(
    val requiredNetWorthCents: Long,
    val titlePt: String,
    val lorePt: String
) {
    SEALED_EGG(0L, "Ovo Estelar Selado", "Choca ao evoluir sua Sabedoria ou atingir R$ 5.000."),
    LUNAR_SLIME(500_000L, "Slime Orvalho Lunar", "Companheiro dócil nascido da sua primeira reserva."),
    STAR_MOOGLE(1_500_000L, "Moogle Estelar", "Mensageiro alado que vigia metas e parcelas."),
    VAULT_OWL(3_500_000L, "Coruja do Cofre", "Guardiã mecânica dourada com visão de longo prazo."),
    SOLSTICE_DRAGON(7_500_000L, "Dragãozinho do Solstício", "Familiar lendário que protege grandes fortunas.")
}

enum class CosmeticSlot {
    COSTUME,
    ACCESSORY,
    MASCOT
}

/**
 * Incremental visual loadout mapped directly from the user's Sabedoria Financeira level range.
 */
data class WisdomVisualLoadout(
    val tierIndex: Int,
    val levelRangeLabel: String,
    val wisdomStage: AvatarWisdomStage,
    val costume: AvatarCostumeTier,
    val accessory: AvatarAccessoryTier,
    val mascot: AvatarMascotTier,
    val auraRingEnabled: Boolean,
    val hasCompanionSlot: Boolean
)

/**
 * Maps a numeric 'Sabedoria Financeira' level into incremental 32-bit RPG visual assets
 * (clothing/costume, accessory, mascot companion, and wisdom aura).
 */
fun mapWisdomLevelToVisualAssets(wisdomLevel: Int): WisdomVisualLoadout = when {
    wisdomLevel >= 7 -> WisdomVisualLoadout(
        tierIndex = 4,
        levelRangeLabel = "Nv. 7+",
        wisdomStage = AvatarWisdomStage.ARQUIMAGO,
        costume = AvatarCostumeTier.CELESTIAL_ARMOR,
        accessory = AvatarAccessoryTier.SOLAR_CROWN,
        mascot = AvatarMascotTier.SOLSTICE_DRAGON,
        auraRingEnabled = true,
        hasCompanionSlot = true
    )
    wisdomLevel >= 5 -> WisdomVisualLoadout(
        tierIndex = 3,
        levelRangeLabel = "Nv. 5–6",
        wisdomStage = AvatarWisdomStage.ARQUIMAGO,
        costume = AvatarCostumeTier.SOLSTICE_COAT,
        accessory = AvatarAccessoryTier.MANA_ORBITAL,
        mascot = AvatarMascotTier.VAULT_OWL,
        auraRingEnabled = true,
        hasCompanionSlot = true
    )
    wisdomLevel >= 3 -> WisdomVisualLoadout(
        tierIndex = 2,
        levelRangeLabel = "Nv. 3–4",
        wisdomStage = AvatarWisdomStage.forLevel(wisdomLevel),
        costume = AvatarCostumeTier.MITHRIL_BRIGANDINE,
        accessory = AvatarAccessoryTier.CLARITY_DIADEM,
        mascot = AvatarMascotTier.STAR_MOOGLE,
        auraRingEnabled = true,
        hasCompanionSlot = true
    )
    wisdomLevel == 2 -> WisdomVisualLoadout(
        tierIndex = 1,
        levelRangeLabel = "Nv. 2",
        wisdomStage = AvatarWisdomStage.ESCRIBA,
        costume = AvatarCostumeTier.LUNAR_MANTLE,
        accessory = AvatarAccessoryTier.CRYSTAL_AMULET,
        mascot = AvatarMascotTier.LUNAR_SLIME,
        auraRingEnabled = true,
        hasCompanionSlot = true
    )
    else -> WisdomVisualLoadout(
        tierIndex = 0,
        levelRangeLabel = "Nv. 1",
        wisdomStage = AvatarWisdomStage.APRENDIZ,
        costume = AvatarCostumeTier.TRAVELER_TUNIC,
        accessory = AvatarAccessoryTier.COPPER_PIN,
        mascot = AvatarMascotTier.SEALED_EGG,
        auraRingEnabled = false,
        hasCompanionSlot = false
    )
}

data class NextCosmeticMilestone(
    val slot: CosmeticSlot,
    val titlePt: String,
    val requiredNetWorthCents: Long,
    val missingCents: Long,
    val progressRatio: Float
)

data class AvatarEvolutionState(
    val wisdomLevel: Int,
    val xpPoints: Long,
    val xpInCurrentLevel: Int,
    val xpToNextLevel: Int,
    val wisdomStage: AvatarWisdomStage,
    val wisdomLoadout: WisdomVisualLoadout,
    val netWorthCents: Long,
    val costume: AvatarCostumeTier,
    val accessory: AvatarAccessoryTier,
    val mascot: AvatarMascotTier,
    val unlockedCostumesCount: Int,
    val unlockedAccessoriesCount: Int,
    val unlockedMascotsCount: Int,
    val totalUnlockedItems: Int,
    val totalAvailableItems: Int,
    val nextMilestone: NextCosmeticMilestone?
)

object AvatarEvolutionEngine {

    val allWisdomRangeLoadouts: List<WisdomVisualLoadout> = listOf(1, 2, 3, 5, 7)
        .map { mapWisdomLevelToVisualAssets(it) }

    fun computeNetWorthCents(profile: UserProfile): Long =
        profile.checkingBalanceCents + profile.emergencyReserveCents - profile.creditCardOpenBillCents

    fun compute(profile: UserProfile): AvatarEvolutionState {
        val xp = profile.xpPoints.coerceAtLeast(0L)
        val wisdomLevel = ((xp / 100L) + 1L).toInt().coerceAtLeast(1)
        val xpInCurrentLevel = (xp % 100L).toInt()
        val xpToNextLevel = (100 - xpInCurrentLevel).coerceAtLeast(1)
        val wisdomStage = AvatarWisdomStage.forLevel(wisdomLevel)
        val wisdomLoadout = mapWisdomLevelToVisualAssets(wisdomLevel)

        val netWorth = computeNetWorthCents(profile)

        val netWorthCostume = AvatarCostumeTier.entries
            .lastOrNull { netWorth >= it.requiredNetWorthCents }
            ?: AvatarCostumeTier.TRAVELER_TUNIC
        val netWorthAccessory = AvatarAccessoryTier.entries
            .lastOrNull { netWorth >= it.requiredNetWorthCents }
            ?: AvatarAccessoryTier.COPPER_PIN
        val netWorthMascot = AvatarMascotTier.entries
            .lastOrNull { netWorth >= it.requiredNetWorthCents }
            ?: AvatarMascotTier.SEALED_EGG

        // Combine Wisdom level loadout + Net Worth progression (highest unlocked visual tier wins)
        val costume = if (wisdomLoadout.costume.ordinal >= netWorthCostume.ordinal) wisdomLoadout.costume else netWorthCostume
        val accessory = if (wisdomLoadout.accessory.ordinal >= netWorthAccessory.ordinal) wisdomLoadout.accessory else netWorthAccessory
        val mascot = if (wisdomLoadout.mascot.ordinal >= netWorthMascot.ordinal) wisdomLoadout.mascot else netWorthMascot

        val unlockedCostumes = AvatarCostumeTier.entries.count {
            netWorth >= it.requiredNetWorthCents || it.ordinal <= wisdomLoadout.costume.ordinal
        }
        val unlockedAccessories = AvatarAccessoryTier.entries.count {
            netWorth >= it.requiredNetWorthCents || it.ordinal <= wisdomLoadout.accessory.ordinal
        }
        val unlockedMascots = AvatarMascotTier.entries.count {
            it != AvatarMascotTier.SEALED_EGG && (netWorth >= it.requiredNetWorthCents || it.ordinal <= wisdomLoadout.mascot.ordinal)
        }

        val allMilestones = buildList {
            AvatarCostumeTier.entries
                .filter { it.requiredNetWorthCents > 0L }
                .forEach { add(Triple(CosmeticSlot.COSTUME, it.titlePt, it.requiredNetWorthCents)) }
            AvatarAccessoryTier.entries
                .filter { it.requiredNetWorthCents > 0L }
                .forEach { add(Triple(CosmeticSlot.ACCESSORY, it.titlePt, it.requiredNetWorthCents)) }
            AvatarMascotTier.entries
                .filter { it.requiredNetWorthCents > 0L }
                .forEach { add(Triple(CosmeticSlot.MASCOT, it.titlePt, it.requiredNetWorthCents)) }
        }.sortedBy { it.third }

        val nextEntry = allMilestones.firstOrNull { netWorth < it.third }
        val prevThreshold = allMilestones.lastOrNull { netWorth >= it.third }?.third ?: 0L

        val nextMilestone = nextEntry?.let { (slot, title, targetCents) ->
            val missing = (targetCents - netWorth).coerceAtLeast(0L)
            val span = (targetCents - prevThreshold).coerceAtLeast(1L)
            val ratio = ((netWorth - prevThreshold).toFloat() / span.toFloat()).coerceIn(0f, 1f)
            NextCosmeticMilestone(
                slot = slot,
                titlePt = title,
                requiredNetWorthCents = targetCents,
                missingCents = missing,
                progressRatio = ratio
            )
        }

        val totalAvailable = allMilestones.size + 2
        val totalUnlocked = unlockedCostumes + unlockedAccessories + unlockedMascots

        return AvatarEvolutionState(
            wisdomLevel = wisdomLevel,
            xpPoints = xp,
            xpInCurrentLevel = xpInCurrentLevel,
            xpToNextLevel = xpToNextLevel,
            wisdomStage = wisdomStage,
            wisdomLoadout = wisdomLoadout,
            netWorthCents = netWorth,
            costume = costume,
            accessory = accessory,
            mascot = mascot,
            unlockedCostumesCount = unlockedCostumes,
            unlockedAccessoriesCount = unlockedAccessories,
            unlockedMascotsCount = unlockedMascots,
            totalUnlockedItems = totalUnlocked,
            totalAvailableItems = totalAvailable,
            nextMilestone = nextMilestone
        )
    }
}
