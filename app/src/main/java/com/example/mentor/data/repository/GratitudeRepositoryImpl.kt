package com.example.mentor.data.repository

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.SaveGratitudeRequest
import com.example.mentor.domain.model.GratitudeEntry
import com.example.mentor.domain.repository.GratitudeRepository
import kotlinx.coroutines.flow.first

class GratitudeRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore
) : GratitudeRepository {

    override suspend fun saveGratitude(content: String): Result<GratitudeEntry> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = SaveGratitudeRequest(content)
            val response = apiService.saveGratitude(token, request)
            Result.success(
                GratitudeEntry(
                    id = response.id,
                    userId = response.userId,
                    content = response.content,
                    createdAt = response.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
