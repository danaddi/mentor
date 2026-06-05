package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.GratitudeEntry
import com.example.mentor.domain.repository.GratitudeRepository
import javax.inject.Inject

class GetGratitudesUseCase @Inject constructor(
    private val gratitudeRepository: GratitudeRepository
) {
    suspend operator fun invoke(): Result<List<GratitudeEntry>> {
        return gratitudeRepository.getGratitudes()
    }
}
