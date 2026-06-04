package com.example.mentor.di

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.local.dao.EmotionDao
import com.example.mentor.data.local.dao.GratitudeDao
import com.example.mentor.data.local.dao.NoteDao
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.repository.*
import com.example.mentor.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: MentorApiService,
        tokenDataStore: TokenDataStore
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, tokenDataStore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        apiService: MentorApiService,
        tokenDataStore: TokenDataStore
    ): ChatRepository {
        return ChatRepositoryImpl(apiService, tokenDataStore)
    }

    @Provides
    @Singleton
    fun provideNotesRepository(
        apiService: MentorApiService,
        tokenDataStore: TokenDataStore,
        noteDao: NoteDao
    ): NotesRepository {
        return NotesRepositoryImpl(apiService, tokenDataStore, noteDao)
    }

    @Provides
    @Singleton
    fun provideEmotionRepository(
        apiService: MentorApiService,
        tokenDataStore: TokenDataStore,
        emotionDao: EmotionDao
    ): EmotionRepository {
        return EmotionRepositoryImpl(apiService, tokenDataStore, emotionDao)
    }

    @Provides
    @Singleton
    fun provideGratitudeRepository(
        apiService: MentorApiService,
        tokenDataStore: TokenDataStore,
        gratitudeDao: GratitudeDao
    ): GratitudeRepository {
        return GratitudeRepositoryImpl(apiService, tokenDataStore, gratitudeDao)
    }
}
