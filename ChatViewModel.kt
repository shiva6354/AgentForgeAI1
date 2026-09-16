package com.agentforge.ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentforge.ai.data.model.ChatMessage
import com.agentforge.ai.domain.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(
    private val repository: GeminiRepository
) : ViewModel() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome-1",
                role = "assistant",
                content = "Hello! I am AgentForge AI, powered by Gemini 3.8 Flash.\n\nI can assist you with:\n- Writing Jetpack Compose UI components\n- Kotlin Coroutines, StateFlow, and Clean Architecture\n- Web HTML/CSS/JavaScript interfaces\n- Debugging stack traces and runtime crashes\n\nWhat would you like to build today?",
                timestamp = timeFormat.format(Date())
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun sendMessage(content: String, projectContext: String = "") {
        val trimmed = content.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        val userMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}",
            role = "user",
            content = trimmed,
            timestamp = timeFormat.format(Date())
        )

        _messages.value = _messages.value + userMsg
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.sendChat(_messages.value, projectContext)
            _isLoading.value = false

            result.onSuccess { reply ->
                val aiMsg = ChatMessage(
                    id = "msg-${System.currentTimeMillis()}",
                    role = "assistant",
                    content = reply,
                    timestamp = timeFormat.format(Date())
                )
                _messages.value = _messages.value + aiMsg
            }.onFailure { err ->
                _error.value = err.message ?: "Failed to connect to AI server."
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                id = "welcome-reset",
                role = "assistant",
                content = "Chat cleared. Ready for your next coding task!",
                timestamp = timeFormat.format(Date())
            )
        )
        _error.value = null
    }
}
