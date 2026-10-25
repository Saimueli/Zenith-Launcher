package com.example.zenithlauncher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperUtil {
    suspend fun setBlackWallpaper(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
                val bitmap = createBitmap(1080, 1920)
                bitmap.eraseColor(Color.BLACK)
                wallpaperManager.setBitmap(bitmap)
            } catch (_: Exception) {
            }
        }
    }
}
