package com.example.mentor.data.repository

import com.example.mentor.data.local.dao.NoteDao
import com.example.mentor.data.local.entity.NoteEntity
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.CreateNoteRequest
import com.example.mentor.domain.model.Note
import com.example.mentor.domain.repository.NotesRepository
import kotlinx.coroutines.flow.first

class NotesRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore,
    private val noteDao: NoteDao
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val response = apiService.getNotes(token)
            val notes = response.map { note ->
                Note(
                    id = note.id,
                    userId = note.userId,
                    chatSessionId = note.chatSessionId,
                    content = note.content,
                    createdAt = note.createdAt
                )
            }
            // Cache in Room
            noteDao.deleteAllNotes()
            noteDao.insertNotes(notes.map { it.toEntity() })
            Result.success(notes)
        } catch (e: Exception) {
            // Fallback to cache
            try {
                val cached = noteDao.getAllNotes()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toDomain() })
                } else {
                    Result.failure(e)
                }
            } catch (dbError: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createNote(content: String, chatSessionId: String?): Result<Note> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = CreateNoteRequest(content, chatSessionId)
            val response = apiService.createNote(token, request)
            val note = Note(
                id = response.id,
                userId = response.userId,
                chatSessionId = response.chatSessionId,
                content = response.content,
                createdAt = response.createdAt
            )
            // Cache in Room
            noteDao.insertNote(note.toEntity())
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Note.toEntity() = NoteEntity(
        id = id,
        userId = userId,
        chatSessionId = chatSessionId,
        content = content,
        createdAt = createdAt
    )

    private fun NoteEntity.toDomain() = Note(
        id = id,
        userId = userId,
        chatSessionId = chatSessionId,
        content = content,
        createdAt = createdAt
    )
}
