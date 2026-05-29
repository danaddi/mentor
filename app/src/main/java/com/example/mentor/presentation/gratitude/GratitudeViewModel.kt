package com.example.mentor.presentation.gratitude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.usecase.SaveGratitudeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GratitudeViewModel @Inject constructor(
    private val saveGratitudeUseCase: SaveGratitudeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GratitudeUiState>(GratitudeUiState.Initial)
    val uiState: StateFlow<GratitudeUiState> = _uiState.asStateFlow()

    fun saveGratitude(content: String) {
        viewModelScope.launch {
            _uiState.value = GratitudeUiState.Loading
            val result = saveGratitudeUseCase(content)
            result.fold(
                onSuccess = { gratitude ->
                    _uiState.value = GratitudeUiState.Success(gratitude)
                },
                onFailure = { error ->
                    _uiState.value = GratitudeUiState.Error(error.message ?: "Failed to save gratitude")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = GratitudeUiState.Initial
    }
}

sealed class GratitudeUiState {
    object Initial : GratitudeUiState()
    object Loading : GratitudeUiState()
    data class Success(val gratitude: com.example.mentor.domain.model.GratitudeEntry) : GratitudeUiState()
    data class Error(val message: String) : GratitudeUiState()
}
