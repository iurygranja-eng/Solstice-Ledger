package com.example.domain

import com.example.data.GuildVault
import com.example.data.UserProfile
import com.example.data.formatBrl

enum class RpgNotificationType {
    DAILY_EXPENSE_REMINDER,
    CONQUEST_ZONE_BREAK_EVEN,
    RESERVE_SAVINGS_GOAL,
    SHIELD_FORTRESS_6M,
    WISDOM_LEVEL_UP,
    COSMETIC_UNLOCKED,
    GUILD_GOAL_REACHED
}

data class RpgNotificationPayload(
    val id: String,
    val notificationId: Int,
    val type: RpgNotificationType,
    val title: String,
    val body: String,
    val openTarget: String,
    val badgeLabel: String
)

data class NotificationMilestoneInput(
    val profile: UserProfile,
    val breakEven: BreakEvenSnapshot,
    val stats: PredictiveStats,
    val evolution: AvatarEvolutionState,
    val guild: GuildVault?
)

object RpgNotificationEngine {

    const val TARGET_FORJA = "forja"
    const val TARGET_TAVERNA = "taverna"
    const val TARGET_GUILDA = "guilda"

    fun buildDailyReminderCopy(
        heroName: String,
        safeToSpendDailyCents: Long,
        languageLevel: Int,
        expensesLoggedToday: Int = 0
    ): RpgNotificationPayload {
        val safeName = heroName.trim().ifBlank { "Viajante" }
        val formattedSafe = formatBrl(safeToSpendDailyCents.coerceAtLeast(0L))

        val (title, body) = when (languageLevel.coerceIn(1, 3)) {
            1 -> {
                if (expensesLoggedToday == 0) {
                    "Missão do Dia: Anote seus gastos, $safeName!" to
                        "Seu limite livre de hoje é $formattedSafe. Registre suas despesas em 10 segundos na Forja para manter seu saldo em dia."
                } else {
                    "Conferência Rápida do Dia, $safeName" to
                        "Você já anotou $expensesLoggedToday lançamento(s) hoje. Sobram $formattedSafe livres hoje — falta alguma comprinha?"
                }
            }
            2 -> {
                if (expensesLoggedToday == 0) {
                    "Crônica Diária: A Forja aguarda, $safeName" to
                        "Orçamento seguro de hoje: $formattedSafe. Lance seus gastos do dia no Livro-Razão para proteger sua meta mensal."
                } else {
                    "Check-in do Caixa: $safeName no controle" to
                        "$expensesLoggedToday registro(s) anotados hoje e $formattedSafe livres no dia. Feche a jornada com o caixa 100% atualizado!"
                }
            }
            else -> {
                if (expensesLoggedToday == 0) {
                    "Quest Diária: A Forja do Solstício Chama!" to
                        "$safeName, sua barra de energia hoje permite $formattedSafe em moedas. Forje seus gastos antes que a névoa apague a memória do dia!"
                } else {
                    "Pergaminho da Noite: Selo de $safeName" to
                        "Você já gravou $expensesLoggedToday feito(s) hoje! Restam $formattedSafe no Limite da Jornada — revise o livro antes do descanso na Taverna."
                }
            }
        }

        return RpgNotificationPayload(
            id = "daily_expense_reminder",
            notificationId = 1001,
            type = RpgNotificationType.DAILY_EXPENSE_REMINDER,
            title = title,
            body = body,
            openTarget = TARGET_FORJA,
            badgeLabel = "MISSÃO DIÁRIA"
        )
    }

    fun buildSampleGoalVictoryCopy(
        heroName: String,
        evolution: AvatarEvolutionState,
        languageLevel: Int
    ): RpgNotificationPayload {
        val safeName = heroName.trim().ifBlank { "Viajante" }
        val (title, body) = when (languageLevel.coerceIn(1, 3)) {
            1 -> "Meta Concluída, $safeName!" to
                "Parabéns! Você atingiu o nível ${evolution.wisdomLevel} de Sabedoria Financeira e desbloqueou ${evolution.costume.titlePt}."
            2 -> "Conquista Desbloqueada: ${evolution.wisdomStage.titlePt}!" to
                "$safeName alcançou Sabedoria Nv.${evolution.wisdomLevel} com patrimônio de ${formatBrl(evolution.netWorthCents)}."
            else -> "Vitória Épica na Taverna, $safeName!" to
                "Os sinos do Solstício tocam! Sabedoria Nv.${evolution.wisdomLevel} conquistada — ${evolution.costume.titlePt} e ${evolution.mascot.titlePt} brilham em seu retrato!"
        }
        return RpgNotificationPayload(
            id = "sample_goal_victory",
            notificationId = 2099,
            type = RpgNotificationType.WISDOM_LEVEL_UP,
            title = title,
            body = body,
            openTarget = TARGET_TAVERNA,
            badgeLabel = "VITÓRIA RPG"
        )
    }

    fun buildAllCandidateMilestones(input: NotificationMilestoneInput): List<RpgNotificationPayload> {
        val profile = input.profile
        val heroName = profile.heroName.trim().ifBlank { "Viajante" }
        val level = profile.languageLevel.coerceIn(1, 3)
        val candidates = mutableListOf<RpgNotificationPayload>()

        // 1. Ponto de Equilíbrio vencido (Zona de Conquista)
        if (input.breakEven.monthlyIncomeCents > 0L && input.breakEven.isInConquestZone) {
            val surplusText = formatBrl(input.breakEven.surplusOrDeficitCents.coerceAtLeast(0L))
            val (title, body) = when (level) {
                1 -> "Contas Pagas: Você entrou no azul!" to
                    "Suas despesas essenciais e faturas do mês estão cobertas com folga de $surplusText."
                2 -> "Ponto de Equilíbrio Vencido!" to
                    "$heroName superou os compromissos do mês! Superávit atual de $surplusText pronto para reserva."
                else -> "Zona de Conquista Desbloqueada!" to
                    "A linha de mana foi ultrapassada, $heroName! +$surplusText de ouro livre para forjar sua reserva."
            }
            candidates += RpgNotificationPayload(
                id = "milestone_conquest_zone",
                notificationId = 2001,
                type = RpgNotificationType.CONQUEST_ZONE_BREAK_EVEN,
                title = title,
                body = body,
                openTarget = TARGET_TAVERNA,
                badgeLabel = "CONQUISTA"
            )
        }

        // 2. Meta de Reserva / Economia atingida
        if (profile.savingsGoalCents > 0L && profile.emergencyReserveCents >= profile.savingsGoalCents) {
            val reserveText = formatBrl(profile.emergencyReserveCents)
            val goalText = formatBrl(profile.savingsGoalCents)
            val (title, body) = when (level) {
                1 -> "Meta de Reserva Atingida!" to
                    "Sua reserva chegou a $reserveText e superou a meta de $goalText. Excelente disciplina!"
                2 -> "Cofre da Meta Preenchido: 100%!" to
                    "$heroName bateu a meta de $goalText (reserva atual em $reserveText). Hora de mirar mais alto!"
                else -> "Tesouro da Meta Conquistado!" to
                    "O cofre transborda luz lunar, $heroName! Meta de $goalText superada com $reserveText guardados."
            }
            candidates += RpgNotificationPayload(
                id = "milestone_savings_goal",
                notificationId = 2002,
                type = RpgNotificationType.RESERVE_SAVINGS_GOAL,
                title = title,
                body = body,
                openTarget = TARGET_TAVERNA,
                badgeLabel = "META BATIDA"
            )
        }

        // 3. Fortaleza de 6 Meses / Escudo Tier >= 4
        if (input.stats.runwayMonths >= 6.0 || input.stats.shieldTier >= 4) {
            val runwayFormatted = String.format(java.util.Locale.US, "%.1f", input.stats.runwayMonths)
            val (title, body) = when (level) {
                1 -> "Reserva de 6 Meses Alcançada!" to
                    "Você tem $runwayFormatted meses de cobertura financeira. Segurança sólida garantida!"
                2 -> "Escudo Nível ${input.stats.shieldTier}: Fortaleza de $runwayFormatted Meses" to
                    "Sua reserva de emergência já protege $runwayFormatted meses do seu custo de vida."
                else -> "Fortaleza do Solstício Ergida!" to
                    "Muralhas de $runwayFormatted meses (Escudo Tier ${input.stats.shieldTier}) protegem $heroName contra qualquer tempestade!"
            }
            candidates += RpgNotificationPayload(
                id = "milestone_fortress_6m",
                notificationId = 2003,
                type = RpgNotificationType.SHIELD_FORTRESS_6M,
                title = title,
                body = body,
                openTarget = TARGET_TAVERNA,
                badgeLabel = "FORTALEZA"
            )
        }

        // 4. Evolução de Nível de Sabedoria Financeira (>= Nv. 2)
        if (input.evolution.wisdomLevel >= 2) {
            val wLvl = input.evolution.wisdomLevel
            val stageTitle = input.evolution.wisdomStage.titlePt
            val (title, body) = when (level) {
                1 -> "Novo Nível de Sabedoria: Nv. $wLvl!" to
                    "Parabéns, $heroName! Você evoluiu para $stageTitle com ${input.evolution.xpPoints} XP."
                2 -> "Sabedoria Financeira Nível $wLvl!" to
                    "Seu perfil subiu para o posto de $stageTitle. Continue registrando e aprendendo no Mapa!"
                else -> "Level Up! Sabedoria Nv. $wLvl — $stageTitle" to
                    "Runas despertam na Taverna! $heroName agora porta o título de $stageTitle (${input.evolution.xpPoints} XP)."
            }
            candidates += RpgNotificationPayload(
                id = "milestone_wisdom_lv_$wLvl",
                notificationId = 2100 + wLvl,
                type = RpgNotificationType.WISDOM_LEVEL_UP,
                title = title,
                body = body,
                openTarget = TARGET_TAVERNA,
                badgeLabel = "LEVEL UP"
            )
        }

        // 5. Novo Traje ou Mascote desbloqueado
        if (input.evolution.costume != AvatarCostumeTier.TRAVELER_TUNIC) {
            val costume = input.evolution.costume
            candidates += RpgNotificationPayload(
                id = "milestone_costume_${costume.name}",
                notificationId = 2200 + costume.ordinal,
                type = RpgNotificationType.COSMETIC_UNLOCKED,
                title = if (level == 1) "Novo Visual Desbloqueado: ${costume.titlePt}" else "Traje Forjado: ${costume.titlePt}!",
                body = "$heroName desbloqueou um novo equipamento de avatar na Taverna: ${costume.lorePt}",
                openTarget = TARGET_TAVERNA,
                badgeLabel = "NOVO TRAJE"
            )
        }

        if (input.evolution.mascot != AvatarMascotTier.SEALED_EGG) {
            val mascot = input.evolution.mascot
            candidates += RpgNotificationPayload(
                id = "milestone_mascot_${mascot.name}",
                notificationId = 2300 + mascot.ordinal,
                type = RpgNotificationType.COSMETIC_UNLOCKED,
                title = if (level == 1) "Novo Companheiro: ${mascot.titlePt}!" else "O Ovo Chocou: ${mascot.titlePt}!",
                body = "$heroName ganhou um novo mascote guardião na Taverna: ${mascot.lorePt}",
                openTarget = TARGET_TAVERNA,
                badgeLabel = "MASCOTE"
            )
        }

        // 6. Meta da Guilda atingida
        val guild = input.guild
        if (guild != null && guild.sharedGoalTargetCents > 0L && guild.sharedGoalSavedCents >= guild.sharedGoalTargetCents) {
            val targetText = formatBrl(guild.sharedGoalTargetCents)
            candidates += RpgNotificationPayload(
                id = "milestone_guild_goal_${guild.id}",
                notificationId = 2401,
                type = RpgNotificationType.GUILD_GOAL_REACHED,
                title = "Meta da Guilda '${guild.guildName}' Atingida!",
                body = if (level == 1) {
                    "O objetivo '${guild.sharedGoalName}' de $targetText foi concluído em conjunto!"
                } else {
                    "Vitória cooperativa! O cofre '${guild.sharedGoalName}' atingiu 100% da meta de $targetText."
                },
                openTarget = TARGET_GUILDA,
                badgeLabel = "GUILDA 100%"
            )
        }

        return candidates
    }

    fun computeCurrentMilestoneIds(input: NotificationMilestoneInput): Set<String> =
        buildAllCandidateMilestones(input).map { it.id }.toSet()

    fun detectAchievedMilestones(
        input: NotificationMilestoneInput,
        alreadySentIds: Set<String>
    ): List<RpgNotificationPayload> =
        buildAllCandidateMilestones(input).filter { it.id !in alreadySentIds }
}
