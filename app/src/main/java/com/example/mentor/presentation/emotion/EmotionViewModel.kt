package com.example.mentor.presentation.emotion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.model.Emotion
import com.example.mentor.domain.usecase.GetEmotionsUseCase
import com.example.mentor.domain.usecase.SaveEmotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EmotionViewModel @Inject constructor(
    private val saveEmotionUseCase: SaveEmotionUseCase,
    private val getEmotionsUseCase: GetEmotionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmotionUiState>(EmotionUiState.Initial)
    val uiState: StateFlow<EmotionUiState> = _uiState.asStateFlow()

    private val _emotions = MutableStateFlow<List<Emotion>>(emptyList())
    val emotions: StateFlow<List<Emotion>> = _emotions.asStateFlow()

    private val today = LocalDate.now()

    private val _startDate = MutableStateFlow(today)
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(today)
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    private val _filteredEmotions = MutableStateFlow<List<Emotion>>(emptyList())
    val filteredEmotions: StateFlow<List<Emotion>> = _filteredEmotions.asStateFlow()

    init {
        loadEmotions()
        setDateRange(today, today)
    }

    fun loadEmotions() {
        viewModelScope.launch {
            val result = getEmotionsUseCase()
            result.fold(
                onSuccess = { entries ->
                    _emotions.value = entries
                },
                onFailure = { /* silently fail, list stays empty */ }
            )
        }
    }

    fun saveEmotion(emotionType: String, intensity: Int) {
        viewModelScope.launch {
            _uiState.value = EmotionUiState.Loading
            val result = saveEmotionUseCase(emotionType, intensity)
            result.fold(
                onSuccess = { emotion ->
                    _uiState.value = EmotionUiState.Success(emotion)
                    // Add to the list
                    _emotions.value = listOf(emotion) + _emotions.value
                    setDateRange(_startDate.value, _endDate.value)
                },
                onFailure = { error ->
                    _uiState.value = EmotionUiState.Error(error.message ?: "Failed to save emotion")
                }
            )
        }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            _startDate.value = start
            _endDate.value = end

            val startIso = "${start}T00:00:00Z"
            val endIso = "${end}T23:59:59Z"
            _filteredEmotions.value = getEmotionsUseCase.getByDateRange(startIso, endIso)
        }
    }

    fun resetState() {
        _uiState.value = EmotionUiState.Initial
    }
}

sealed class EmotionUiState {
    object Initial : EmotionUiState()
    object Loading : EmotionUiState()
    data class Success(val emotion: Emotion) : EmotionUiState()
    data class Error(val message: String) : EmotionUiState()
}
