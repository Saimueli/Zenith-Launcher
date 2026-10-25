package com.example.zenithlauncher.ui

data class AppInfo(
    val label: String,
    val packageName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false
)
