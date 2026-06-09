package com.example.mentor.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentor.domain.model.Note
import com.example.mentor.domain.usecase.CreateNoteUseCase
import com.example.mentor.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val createNoteUseCase: CreateNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Initial)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = NotesUiState.Loading
            val result = getNotesUseCase()
            result.fold(
                onSuccess = { notes ->
                    _uiState.value = NotesUiState.Success(notes)
                },
                onFailure = { error ->
                    _uiState.value = NotesUiState.Error(error.message ?: "Failed to load notes")
                }
            )
        }
    }

    fun createNote(content: String, chatSessionId: String? = null) {
        viewModelScope.launch {
            val currentNotes = (_uiState.value as? NotesUiState.Success)?.notes ?: emptyList()
            _uiState.value = NotesUiState.Success(currentNotes)

            val result = createNoteUseCase(content, chatSessionId)
            result.fold(
                onSuccess = { note ->
                    val updatedNotes = currentNotes + note
                    _uiState.value = NotesUiState.Success(updatedNotes)
                },
                onFailure = { error ->
                    _uiState.value = NotesUiState.Error(error.message ?: "Failed to create note")
                }
            )
        }
    }

    fun selectNote(note: Note) {
        _selectedNote.value = note
    }

    fun clearSelectedNote() {
        _selectedNote.value = null
    }

    fun refresh() {
        loadNotes()
    }
}

sealed class NotesUiState {
    object Initial : NotesUiState()
    object Loading : NotesUiState()
    data class Success(val notes: List<com.example.mentor.domain.model.Note>) : NotesUiState()
    data class Error(val message: String) : NotesUiState()
}
