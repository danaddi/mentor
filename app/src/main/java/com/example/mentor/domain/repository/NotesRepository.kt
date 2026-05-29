package com.example.mentor.domain.repository

import com.example.mentor.domain.model.Note

interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(content: String, chatSessionId: String?): Result<Note>
}
