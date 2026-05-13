package com.tonapps.tonkeeper.manager.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

class WallpaperManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "wallpaper_prefs"
        private const val KEY_WALLPAPER_SET = "wallpaper_set"
        private const val KEY_DIM_LEVEL = "dim_level"
        private const val WALLPAPER_FILE = "wallpaper.jpg"
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    val isWallpaperSet: Boolean
        get() = prefs.getBoolean(KEY_WALLPAPER_SET, false) && getWallpaperFile().exists()

    var dimLevel: Float
        get() = prefs.getFloat(KEY_DIM_LEVEL, 0.4f)
        set(value) {
            prefs.edit().putFloat(KEY_DIM_LEVEL, value.coerceIn(0f, 0.9f)).apply()
        }

    fun setWallpaper(uri: Uri): Boolean {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return false
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            if (bitmap == null) return false

            val file = getWallpaperFile()
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            prefs.edit().putBoolean(KEY_WALLPAPER_SET, true).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getWallpaperBitmap(): Bitmap? {
        val file = getWallpaperFile()
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    fun removeWallpaper() {
        getWallpaperFile().delete()
        prefs.edit().putBoolean(KEY_WALLPAPER_SET, false).apply()
    }

    private fun getWallpaperFile(): File {
        return File(context.filesDir, WALLPAPER_FILE)
    }
}
