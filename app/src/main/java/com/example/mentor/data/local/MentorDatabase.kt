package com.example.mentor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mentor.data.local.dao.EmotionDao
import com.example.mentor.data.local.dao.GratitudeDao
import com.example.mentor.data.local.dao.MessageDao
import com.example.mentor.data.local.dao.NoteDao
import com.example.mentor.data.local.entity.EmotionEntity
import com.example.mentor.data.local.entity.GratitudeEntity
import com.example.mentor.data.local.entity.MessageEntity
import com.example.mentor.data.local.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        EmotionEntity::class,
        GratitudeEntity::class,
        MessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MentorDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun emotionDao(): EmotionDao
    abstract fun gratitudeDao(): GratitudeDao
    abstract fun messageDao(): MessageDao
}
