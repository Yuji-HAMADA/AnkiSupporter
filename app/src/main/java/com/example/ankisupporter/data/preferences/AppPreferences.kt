package com.example.ankisupporter.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anki_supporter_settings")

class AppPreferences(private val context: Context) {

    private object Keys {
        val ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        val OLLAMA_HOST = stringPreferencesKey("ollama_host")
        val OLLAMA_MODEL = stringPreferencesKey("ollama_model")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val TTS_RATE = floatPreferencesKey("tts_speech_rate")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val providerStr = prefs[Keys.ACTIVE_PROVIDER] ?: AiProvider.OLLAMA.name
        val provider = try {
            AiProvider.valueOf(providerStr)
        } catch (e: Exception) {
            AiProvider.OLLAMA
        }

        AppSettings(
            activeProvider = provider,
            ollamaHost = prefs[Keys.OLLAMA_HOST] ?: "http://10.0.2.2:11434",
            ollamaModel = prefs[Keys.OLLAMA_MODEL] ?: "llama3.2",
            geminiApiKey = prefs[Keys.GEMINI_API_KEY] ?: "",
            geminiModel = prefs[Keys.GEMINI_MODEL] ?: "gemini-1.5-flash",
            ttsSpeechRate = prefs[Keys.TTS_RATE] ?: 1.0f
        )
    }

    suspend fun updateActiveProvider(provider: AiProvider) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_PROVIDER] = provider.name
        }
    }

    suspend fun updateOllamaSettings(host: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OLLAMA_HOST] = host
            prefs[Keys.OLLAMA_MODEL] = model
        }
    }

    suspend fun updateGeminiSettings(apiKey: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GEMINI_API_KEY] = apiKey
            prefs[Keys.GEMINI_MODEL] = model
        }
    }

    suspend fun updateTtsSpeechRate(rate: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TTS_RATE] = rate
        }
    }
}

