package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Note
import com.example.mentor.domain.repository.NotesRepository
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(): Result<List<Note>> {
        return notesRepository.getNotes()
    }
}
