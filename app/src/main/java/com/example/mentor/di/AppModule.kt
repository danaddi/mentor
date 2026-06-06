package com.example.mentor.di

import android.content.Context
import androidx.room.Room
import com.example.mentor.data.local.MentorDatabase
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.local.dao.EmotionDao
import com.example.mentor.data.local.dao.GratitudeDao
import com.example.mentor.data.local.dao.MessageDao
import com.example.mentor.data.local.dao.NoteDao
import com.example.mentor.data.remote.api.MentorApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    @Provides
    @Singleton
    fun provideMentorApiService(client: HttpClient): MentorApiService {
        return MentorApiService(client)
    }

    @Provides
    @Singleton
    fun provideTokenDataStore(@ApplicationContext context: Context): TokenDataStore {
        return TokenDataStore(context)
    }

    @Provides
    @Singleton
    fun provideMentorDatabase(@ApplicationContext context: Context): MentorDatabase {
        return Room.databaseBuilder(
            context,
            MentorDatabase::class.java,
            "mentor_database"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: MentorDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideEmotionDao(database: MentorDatabase): EmotionDao {
        return database.emotionDao()
    }

    @Provides
    @Singleton
    fun provideGratitudeDao(database: MentorDatabase): GratitudeDao {
        return database.gratitudeDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: MentorDatabase): MessageDao {
        return database.messageDao()
    }
}
