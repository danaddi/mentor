package com.example.mentor.domain.repository

import com.example.mentor.domain.model.GratitudeEntry

interface GratitudeRepository {
    suspend fun saveGratitude(content: String): Result<GratitudeEntry>
}
