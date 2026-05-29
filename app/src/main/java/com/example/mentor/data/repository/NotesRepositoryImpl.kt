package com.example.mentor.data.repository

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.CreateNoteRequest
import com.example.mentor.domain.model.Note
import com.example.mentor.domain.repository.NotesRepository
import kotlinx.coroutines.flow.first

class NotesRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val response = apiService.getNotes(token)
            Result.success(
                response.map { note ->
                    Note(
                        id = note.id,
                        userId = note.userId,
                        chatSessionId = note.chatSessionId,
                        content = note.content,
                        createdAt = note.createdAt
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNote(content: String, chatSessionId: String?): Result<Note> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = CreateNoteRequest(content, chatSessionId)
            val response = apiService.createNote(token, request)
            Result.success(
                Note(
                    id = response.id,
                    userId = response.userId,
                    chatSessionId = response.chatSessionId,
                    content = response.content,
                    createdAt = response.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
