package com.example.mentor.presentation.emotion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.usecase.SaveEmotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmotionViewModel @Inject constructor(
    private val saveEmotionUseCase: SaveEmotionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmotionUiState>(EmotionUiState.Initial)
    val uiState: StateFlow<EmotionUiState> = _uiState.asStateFlow()

    fun saveEmotion(emotionType: String, intensity: Int) {
        viewModelScope.launch {
            _uiState.value = EmotionUiState.Loading
            val result = saveEmotionUseCase(emotionType, intensity)
            result.fold(
                onSuccess = { emotion ->
                    _uiState.value = EmotionUiState.Success(emotion)
                },
                onFailure = { error ->
                    _uiState.value = EmotionUiState.Error(error.message ?: "Failed to save emotion")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = EmotionUiState.Initial
    }
}

sealed class EmotionUiState {
    object Initial : EmotionUiState()
    object Loading : EmotionUiState()
    data class Success(val emotion: com.example.mentor.domain.model.Emotion) : EmotionUiState()
    data class Error(val message: String) : EmotionUiState()
}
