package com.example.mentor.presentation.chathistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.model.ChatSession
import com.example.mentor.domain.usecase.GetChatSessionsUseCase
import com.example.mentor.domain.usecase.GetMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionWithTitle(
    val session: ChatSession,
    val title: String
)

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val getChatSessionsUseCase: GetChatSessionsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatHistoryUiState>(ChatHistoryUiState.Initial)
    val uiState: StateFlow<ChatHistoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = ChatHistoryUiState.Loading
            val result = getChatSessionsUseCase()
            result.fold(
                onSuccess = { sessions ->
                    val sessionsWithTitles = sessions.map { session ->
                        val title = getSessionTitle(session.id)
                        SessionWithTitle(session, title)
                    }
                    _uiState.value = ChatHistoryUiState.Success(sessionsWithTitles)
                },
                onFailure = { error ->
                    _uiState.value = ChatHistoryUiState.Error(error.message ?: "Failed to load sessions")
                }
            )
        }
    }

    private suspend fun getSessionTitle(sessionId: String): String {
        return try {
            val result = getMessagesUseCase(sessionId)
            result.fold(
                onSuccess = { messages ->
                    val firstUserMessage = messages.firstOrNull { it.role == "user" }
                    if (firstUserMessage != null) {
                        val words = firstUserMessage.content.split(" ").take(5)
                        val title = words.joinToString(" ")
                        if (title.length > 40) title.substring(0, 37) + "..." else title
                    } else {
                        "Новый чат"
                    }
                },
                onFailure = {
                    "Новый чат"
                }
            )
        } catch (e: Exception) {
            "Новый чат"
        }
    }

    fun refresh() {
        loadSessions()
    }
}

sealed class ChatHistoryUiState {
    object Initial : ChatHistoryUiState()
    object Loading : ChatHistoryUiState()
    data class Success(val sessions: List<SessionWithTitle>) : ChatHistoryUiState()
    data class Error(val message: String) : ChatHistoryUiState()
}
