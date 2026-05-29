package com.example.mentor.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.model.Message
import com.example.mentor.domain.usecase.CreateChatSessionUseCase
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
    private val sendMessageUseCase: SendMessageUseCase
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
                    _uiState.value = ChatUiState.ChatReady(session.id, emptyList())
                },
                onFailure = { error ->
                    _uiState.value = ChatUiState.Error(error.message ?: "Failed to create session")
                }
            )
        }
    }

    fun sendMessage(content: String) {
        val sessionId = currentSessionId
        if (sessionId == null || content.isBlank()) return

        viewModelScope.launch {
            val currentMessages = (_uiState.value as? ChatUiState.ChatReady)?.messages ?: emptyList()
            _uiState.value = ChatUiState.ChatReady(sessionId, currentMessages)

            val result = sendMessageUseCase(sessionId, content)
            result.fold(
                onSuccess = { (userMessage, aiMessage) ->
                    val updatedMessages = currentMessages + userMessage + aiMessage
                    _uiState.value = ChatUiState.ChatReady(sessionId, updatedMessages)
                },
                onFailure = { error ->
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
