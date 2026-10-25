package com.example.zenithlauncher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey
    val packageName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false
)
