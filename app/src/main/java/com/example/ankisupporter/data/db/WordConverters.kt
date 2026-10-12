package com.example.ankisupporter.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WordConverters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "[]" else json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            // カンマ区切りのフォールバック
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
