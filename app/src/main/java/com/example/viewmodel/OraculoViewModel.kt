package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repo.FinancialContextSnapshot
import com.example.data.repo.OracleAction
import com.example.data.repo.OracleHistoryTurn
import com.example.data.repo.OracleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class OracleChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val actions: List<OracleAction> = emptyList(),
    val evidenceBadges: List<String> = emptyList(),
    val isStreaming: Boolean = false
)

class OraculoViewModel(
    private val oracleRepository: OracleRepository = OracleRepository()
) : ViewModel() {

    private fun initialGreeting(): OracleChatMessage = OracleChatMessage(
        isUser = false,
        text = "Saudações, viajante! Sou **O Oráculo**, seu estrategista de **Finanças, Economia, Estatística e RPG**. Pesquiso seu livro-razão (média, mediana, desvio padrão e Pareto) e traduzo Selic, parcelas ou reservas em jogadas simples. O que vamos investigar hoje?"
    )

    private val _messages = MutableStateFlow(listOf(initialGreeting()))
    val messages: StateFlow<List<OracleChatMessage>> = _messages.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    fun clearConversation() {
        if (_isStreaming.value) return
        _messages.value = listOf(initialGreeting())
    }

    fun sendMessage(prompt: String, snapshot: FinancialContextSnapshot) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isStreaming.value) return

        // Capture multi-turn conversation history prior to appending the new prompt
        val history = _messages.value
            .drop(1) // skip static greeting
            .filter { !it.isStreaming && it.text.isNotBlank() }
            .map { OracleHistoryTurn(isUser = it.isUser, text = it.text) }

        val userMsg = OracleChatMessage(isUser = true, text = trimmed)
        val assistantMsgId = UUID.randomUUID().toString()
        val placeholderMsg = OracleChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Consultando estrelas, estatísticas e seu livro-razão...",
            isStreaming = true
        )

        _messages.update { it + userMsg + placeholderMsg }
        _isStreaming.value = true

        viewModelScope.launch {
            oracleRepository.streamOracleAnswer(trimmed, history, snapshot).collect { chunk ->
                when (chunk) {
                    is OracleRepository.StreamChunk.Streaming -> {
                        _messages.update { list ->
                            list.map { msg ->
                                if (msg.id == assistantMsgId) {
                                    msg.copy(
                                        text = chunk.text,
                                        actions = chunk.actions,
                                        evidenceBadges = chunk.evidenceBadges,
                                        isStreaming = true
                                    )
                                } else msg
                            }
                        }
                    }
                    is OracleRepository.StreamChunk.Completed -> {
                        _messages.update { list ->
                            list.map { msg ->
                                if (msg.id == assistantMsgId) {
                                    msg.copy(
                                        text = chunk.text,
                                        actions = chunk.actions,
                                        evidenceBadges = chunk.evidenceBadges,
                                        isStreaming = false
                                    )
                                } else msg
                            }
                        }
                        _isStreaming.value = false
                    }
                }
            }
        }
    }
}
