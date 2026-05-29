package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.User
import com.example.mentor.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.register(email, password)
    }
}
