package com.example.mentor.presentation.chathistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.usecase.GetChatSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val getChatSessionsUseCase: GetChatSessionsUseCase
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
                    _uiState.value = ChatHistoryUiState.Success(sessions)
                },
                onFailure = { error ->
                    _uiState.value = ChatHistoryUiState.Error(error.message ?: "Failed to load sessions")
                }
            )
        }
    }

    fun refresh() {
        loadSessions()
    }
}

sealed class ChatHistoryUiState {
    object Initial : ChatHistoryUiState()
    object Loading : ChatHistoryUiState()
    data class Success(val sessions: List<com.example.mentor.domain.model.ChatSession>) : ChatHistoryUiState()
    data class Error(val message: String) : ChatHistoryUiState()
}
