package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Note
import com.example.mentor.domain.repository.NotesRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(content: String, chatSessionId: String? = null): Result<Note> {
        return notesRepository.createNote(content, chatSessionId)
    }
}
