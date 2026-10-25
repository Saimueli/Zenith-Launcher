package com.example.zenithlauncher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<AppPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(preference: AppPreferenceEntity)

    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName")
    suspend fun getPreference(packageName: String): AppPreferenceEntity?
}
