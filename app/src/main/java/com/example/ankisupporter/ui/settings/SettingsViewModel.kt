package com.example.ankisupporter.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankisupporter.data.ai.AiRepository
import com.example.ankisupporter.data.preferences.AppPreferences
import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.speech.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProvider: AiProvider = AiProvider.OLLAMA,
    val ollamaHost: String = "http://192.168.11.19:11434",
    val ollamaModel: String = "gemma2:9b",
    val geminiApiKey: String = com.example.ankisupporter.BuildConfig.DEFAULT_GEMINI_API_KEY,
    val geminiModel: String = "gemini-3.1-flash-lite",
    val ttsSpeechRate: Float = 1.0f,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val isSuccess: Boolean? = null
)

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val aiRepository: AiRepository,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    activeProvider = settings.activeProvider,
                    ollamaHost = settings.ollamaHost,
                    ollamaModel = settings.ollamaModel,
                    geminiApiKey = settings.geminiApiKey,
                    geminiModel = settings.geminiModel,
                    ttsSpeechRate = settings.ttsSpeechRate
                )
            }
        }
    }

    fun selectProvider(provider: AiProvider) {
        viewModelScope.launch {
            preferences.updateActiveProvider(provider)
        }
    }

    fun updateOllamaHost(host: String) {
        _uiState.value = _uiState.value.copy(ollamaHost = host)
    }

    fun updateOllamaModel(model: String) {
        _uiState.value = _uiState.value.copy(ollamaModel = model)
    }

    fun saveOllamaSettings() {
        viewModelScope.launch {
            preferences.updateOllamaSettings(
                host = _uiState.value.ollamaHost,
                model = _uiState.value.ollamaModel
            )
        }
    }

    fun updateGeminiApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(geminiApiKey = apiKey)
    }

    fun updateGeminiModel(model: String) {
        _uiState.value = _uiState.value.copy(geminiModel = model)
    }

    fun saveGeminiSettings() {
        viewModelScope.launch {
            preferences.updateGeminiSettings(
                apiKey = _uiState.value.geminiApiKey,
                model = _uiState.value.geminiModel
            )
        }
    }

    fun updateTtsSpeechRate(rate: Float) {
        viewModelScope.launch {
            preferences.updateTtsSpeechRate(rate)
            ttsManager.setSpeechRate(rate)
        }
    }

    fun testTts() {
        ttsManager.speakEnglish("Hello! This is a test of English pronunciation.")
    }

    fun testConnection(provider: AiProvider) {
        viewModelScope.launch {
            // まず現在入力中の値を保存
            if (provider == AiProvider.OLLAMA) {
                saveOllamaSettings()
            } else {
                saveGeminiSettings()
            }

            _uiState.value = _uiState.value.copy(
                isTesting = true,
                testResult = null,
                isSuccess = null
            )

            val result = aiRepository.testConnection(provider)
            result.fold(
                onSuccess = { msg ->
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testResult = msg,
                        isSuccess = true
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testResult = "接続失敗: ${err.localizedMessage}",
                        isSuccess = false
                    )
                }
            )
        }
    }
}

