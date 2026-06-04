package com.example.mentor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mentor.data.local.entity.EmotionEntity

@Dao
interface EmotionDao {
    @Query("SELECT * FROM emotions ORDER BY createdAt DESC")
    suspend fun getAllEmotions(): List<EmotionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmotion(emotion: EmotionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmotions(emotions: List<EmotionEntity>)

    @Query("DELETE FROM emotions")
    suspend fun deleteAllEmotions()
}
