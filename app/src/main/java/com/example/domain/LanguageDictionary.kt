package com.example.domain

enum class TermKey {
    FREE_BUDGET,
    LOCKED_CARD,
    CHECKING_BALANCE,
    BREAK_EVEN,
    RESERVE_SHIELD,
    ORACLE_TITLE,
    SAFE_TO_SPEND,
    BURN_RATE,
    PROJECTED_CLOSE,
    FUTURE_COMMITMENT,
    RUNWAY,
    FORJA_TITLE,
    MAPA_TITLE,
    TAVERNA_TITLE,
    CONQUEST_ZONE,
    GUILDA_VAULT
}

data class TermDisplay(
    val label: String,
    val tooltipLevel3: String
)

object LanguageDictionary {

    fun get(key: TermKey, level: Int): TermDisplay {
        val clamped = level.coerceIn(1, 3)
        return when (key) {
            TermKey.FREE_BUDGET -> when (clamped) {
                1 -> TermDisplay("Orçamento Livre do Mês", "Valor disponível para gastar no mês dentro dos limites.")
                2 -> TermDisplay("HP do Mês (Orçamento Livre)", "Valor disponível para gastar no mês dentro dos limites.")
                else -> TermDisplay("HP da Campanha Mensal", "Quanto ainda resta do seu orçamento planejado para este mês.")
            }

            TermKey.LOCKED_CARD -> when (clamped) {
                1 -> TermDisplay("Comprometido no Cartão", "Soma das compras parceladas e fatura aberta no crédito.")
                2 -> TermDisplay("Mana Bloqueada (Parcelas Futuras)", "Soma das compras parceladas e fatura aberta no crédito.")
                else -> TermDisplay("Mana Selada em Cristais", "Parcelas futuras no cartão que já comprometem sua renda dos próximos meses.")
            }

            TermKey.CHECKING_BALANCE -> when (clamped) {
                1 -> TermDisplay("Dinheiro Disponível Hoje", "Saldo líquido real na conta corrente, débito e Pix.")
                2 -> TermDisplay("Bolsas de Ouro (Saldo em Conta)", "Saldo líquido real na conta corrente, débito e Pix.")
                else -> TermDisplay("Ouro Líquido no Baú", "Dinheiro vivo disponível agora na sua conta bancária (regime de caixa).")
            }

            TermKey.BREAK_EVEN -> when (clamped) {
                1 -> TermDisplay("Mínimo para Pagar as Contas", "Total necessário para cobrir custos fixos, faturas e essenciais.")
                2 -> TermDisplay("Linha de Equilíbrio (Ponto de Equilíbrio)", "Total necessário para cobrir custos fixos, faturas e essenciais.")
                else -> TermDisplay("Selo de Sobrevivência", "Meta mínima de receita para cobrir todas as contas básicas antes de lucrar.")
            }

            TermKey.RESERVE_SHIELD -> when (clamped) {
                1 -> TermDisplay("Reserva de Segurança", "Dinheiro guardado para emergências e tranquilidade.")
                2 -> TermDisplay("Escudo de Reserva", "Dinheiro guardado para emergências e tranquilidade.")
                else -> TermDisplay("Escudo Cristalino", "Sua reserva de emergência medida em quantos meses de vida básica ela sustenta.")
            }

            TermKey.ORACLE_TITLE -> when (clamped) {
                1 -> TermDisplay("Assistente Financeiro", "Consultor inteligente conectado aos seus números reais.")
                2 -> TermDisplay("Oráculo Financeiro (IA)", "Consultor inteligente conectado aos seus números reais.")
                else -> TermDisplay("Oráculo da Guilda", "IA estrategista que lê suas contas, faturas e projeções em tempo real.")
            }

            TermKey.SAFE_TO_SPEND -> when (clamped) {
                1 -> TermDisplay("Limite Seguro Diário", "Quanto você pode gastar hoje sem prejudicar suas contas ou metas.")
                2 -> TermDisplay("Ataque Seguro Hoje (Limite Diário)", "Quanto você pode gastar hoje sem prejudicar suas contas ou metas.")
                else -> TermDisplay("Cota Diária de Energia", "Valor exato seguro para gastar hoje mantendo a meta de poupança intacta.")
            }

            TermKey.BURN_RATE -> when (clamped) {
                1 -> TermDisplay("Ritmo de Gasto", "Compara quanto já foi gasto com quantos dias do mês passaram.")
                2 -> TermDisplay("Velocidade de Queima (Burn Rate)", "Compara quanto já foi gasto com quantos dias do mês passaram.")
                else -> TermDisplay("Consumo de Vigor", "Mede se seu gasto está mais rápido do que a passagem dos dias do mês.")
            }

            TermKey.PROJECTED_CLOSE -> when (clamped) {
                1 -> TermDisplay("Saldo Projetado no Fim do Mês", "Estimativa de quanto sobrará na conta no último dia do mês.")
                2 -> TermDisplay("Fechamento Previsto (Projeção)", "Estimativa de quanto sobrará na conta no último dia do mês.")
                else -> TermDisplay("Tesouro ao Fim da Jornada", "Quanto deve sobrar no seu baú no fim do mês mantendo o ritmo atual.")
            }

            TermKey.FUTURE_COMMITMENT -> when (clamped) {
                1 -> TermDisplay("Renda Já Comprometida", "Percentual dos próximos meses já tomado por fixos e parcelas.")
                2 -> TermDisplay("Comprometimento Futuro (Parcelas + Fixos)", "Percentual dos próximos meses já tomado por fixos e parcelas.")
                else -> TermDisplay("Grilhões dos Próximos Ciclos", "Porcentagem da sua renda futura que já nasce comprometida antes do mês começar.")
            }

            TermKey.RUNWAY -> when (clamped) {
                1 -> TermDisplay("Meses de Fôlego da Reserva", "Quantos meses você se mantém usando apenas sua reserva atual.")
                2 -> TermDisplay("Horizonte de Segurança (Runway)", "Quantos meses você se mantém usando apenas sua reserva atual.")
                else -> TermDisplay("Autonomia da Fortaleza", "Tempo em meses que sua reserva sustenta seus custos essenciais sem renda nova.")
            }

            TermKey.FORJA_TITLE -> when (clamped) {
                1 -> TermDisplay("Novo Lançamento", "Registrar gasto, receita, transferência ou importar extrato.")
                2 -> TermDisplay("Forja de Lançamentos", "Registrar gasto, receita, transferência ou importar extrato.")
                else -> TermDisplay("Forja do Destino", "Canal rápido para registrar movimentações ou importar pergaminhos OFX/CSV.")
            }

            TermKey.MAPA_TITLE -> when (clamped) {
                1 -> TermDisplay("Projeções e Casal", "Indicadores futuros, parcelas, tags, casal e lições.")
                2 -> TermDisplay("Mapa das Estrelas (Projeções)", "Indicadores futuros, parcelas, tags, Guilda e Grimório.")
                else -> TermDisplay("Mapa das Estrelas", "Observatório do Sistema 2 com previsões, parcelas seladas, Guilda e Grimório.")
            }

            TermKey.TAVERNA_TITLE -> when (clamped) {
                1 -> TermDisplay("Resumo", "Visão rápida para decisões financeiras do dia a dia.")
                2 -> TermDisplay("Taverna Central", "Visão rápida para decisões financeiras do dia a dia.")
                else -> TermDisplay("Taverna Central", "Painel tático de uso diário para guiar suas escolhas sem esforço.")
            }

            TermKey.CONQUEST_ZONE -> when (clamped) {
                1 -> TermDisplay("Zona Positiva (Livre para Metas)", "Receita acima do básico do mês, livre para investir ou realizar sonhos.")
                2 -> TermDisplay("Zona de Conquista (Acima do Equilíbrio)", "Receita acima do básico do mês, livre para investir ou realizar sonhos.")
                else -> TermDisplay("Zona de Ascensão Solar", "Excedente puro após vencer custos fixos, faturas e gastos essenciais.")
            }

            TermKey.GUILDA_VAULT -> when (clamped) {
                1 -> TermDisplay("Finanças do Casal", "Despesas conjuntas, divisão justa e acerto do mês.")
                2 -> TermDisplay("Cofre da Guilda (Casal)", "Despesas conjuntas, divisão justa e acerto do mês.")
                else -> TermDisplay("Cofre da Guilda", "Aliança compartilhada onde os dois heróis equilibram custos e metas comuns.")
            }
        }
    }

    fun levelLabel(level: Int): String = when (level.coerceIn(1, 3)) {
        1 -> "Nível 1 • Fácil"
        2 -> "Nível 2 • Médio"
        else -> "Nível 3 • RPG"
    }
}
