package com.example.mentor.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.model.Message
import com.example.mentor.domain.usecase.CreateChatSessionUseCase
import com.example.mentor.domain.usecase.GetMessagesUseCase
import com.example.mentor.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val createChatSessionUseCase: CreateChatSessionUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null

    fun createSession(sectionType: String = "general") {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            val result = createChatSessionUseCase(sectionType)
            result.fold(
                onSuccess = { session ->
                    currentSessionId = session.id
                    loadMessages(session.id)
                },
                onFailure = { error ->
                    _uiState.value = ChatUiState.Error(error.message ?: "Failed to create session")
                }
            )
        }
    }

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            val result = getMessagesUseCase(sessionId)
            result.fold(
                onSuccess = { messages ->
                    _uiState.value = ChatUiState.ChatReady(sessionId, messages)
                },
                onFailure = { error ->
                    // If loading cached messages fails, start with empty list
                    _uiState.value = ChatUiState.ChatReady(sessionId, emptyList())
                }
            )
        }
    }

    fun loadExistingSession(sessionId: String) {
        currentSessionId = sessionId
        loadMessages(sessionId)
    }

    fun sendMessage(content: String) {
        val sessionId = currentSessionId
        if (sessionId == null || content.isBlank()) return

        viewModelScope.launch {
            val currentMessages = (_uiState.value as? ChatUiState.ChatReady)?.messages ?: emptyList()
            
            // Create optimistic user message
            val optimisticUserMessage = Message(
                id = "temp_${System.currentTimeMillis()}",
                role = "user",
                content = content,
                createdAt = java.time.Instant.now().toString()
            )
            
            // Show loading state with optimistic message
            _uiState.value = ChatUiState.ChatReady(sessionId, currentMessages + optimisticUserMessage)

            val result = sendMessageUseCase(sessionId, content)
            result.fold(
                onSuccess = { (userMessage, aiMessage) ->
                    // Replace optimistic message with real one and add AI response
                    val updatedMessages = currentMessages + userMessage + aiMessage
                    _uiState.value = ChatUiState.ChatReady(sessionId, updatedMessages)
                },
                onFailure = { error ->
                    // Remove optimistic message and show error
                    _uiState.value = ChatUiState.ChatReady(sessionId, currentMessages)
                    _uiState.value = ChatUiState.Error(error.message ?: "Failed to send message")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = ChatUiState.Initial
        currentSessionId = null
    }
}

sealed class ChatUiState {
    object Initial : ChatUiState()
    object Loading : ChatUiState()
    data class ChatReady(val sessionId: String, val messages: List<Message>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}
