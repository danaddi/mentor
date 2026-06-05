package com.example.mentor.data.repository

import com.example.mentor.data.local.dao.GratitudeDao
import com.example.mentor.data.local.entity.GratitudeEntity
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.SaveGratitudeRequest
import com.example.mentor.domain.model.GratitudeEntry
import com.example.mentor.domain.repository.GratitudeRepository
import kotlinx.coroutines.flow.first

class GratitudeRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore,
    private val gratitudeDao: GratitudeDao
) : GratitudeRepository {

    override suspend fun saveGratitude(content: String): Result<GratitudeEntry> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = SaveGratitudeRequest(content)
            val response = apiService.saveGratitude(token, request)
            val entry = GratitudeEntry(
                id = response.id,
                userId = response.userId,
                content = response.content,
                createdAt = response.createdAt
            )
            // Cache in Room
            gratitudeDao.insertGratitude(entry.toEntity())
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGratitudes(): Result<List<GratitudeEntry>> {
        return try {
            val cached = gratitudeDao.getAllGratitudes()
            Result.success(cached.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun GratitudeEntry.toEntity() = GratitudeEntity(
        id = id,
        userId = userId,
        content = content,
        createdAt = createdAt
    )

    private fun GratitudeEntity.toDomain() = GratitudeEntry(
        id = id,
        userId = userId,
        content = content,
        createdAt = createdAt
    )
}
