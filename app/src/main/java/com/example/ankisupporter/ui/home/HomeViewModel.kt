package com.example.ankisupporter.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankisupporter.data.ai.AiRepository
import com.example.ankisupporter.data.db.WordRepository
import com.example.ankisupporter.data.preferences.AppPreferences
import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.model.GeneratedCard
import com.example.ankisupporter.speech.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HomeUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val generatedCard: GeneratedCard? = null,
    val activeProvider: AiProvider = AiProvider.OLLAMA,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaved: Boolean = false
)

class HomeViewModel(
    private val aiRepository: AiRepository,
    private val wordRepository: WordRepository,
    private val preferences: AppPreferences,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 設定されたアクティブプロバイダーを購読
        viewModelScope.launch {
            preferences.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(activeProvider = settings.activeProvider)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, errorMessage = null)
    }

    fun generateSentence() {
        val input = _uiState.value.inputText.trim()
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "単語またはテキストを入力してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                isSaved = false
            )

            val result = aiRepository.generateExample(input)
            result.fold(
                onSuccess = { card ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedCard = card,
                        errorMessage = null
                    )
                    // 検索履歴（最大100件）に自動記録
                    viewModelScope.launch {
                        try {
                            wordRepository.recordSearchHistory(card)
                        } catch (_: Exception) {
                            // 履歴記録の失敗はUIエラーとして遮断しない
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "生成に失敗しました: ${error.localizedMessage ?: "接続エラー"}"
                    )
                }
            )
        }
    }

    fun speakWord() {
        val word = _uiState.value.generatedCard?.word ?: _uiState.value.inputText
        if (word.isNotBlank()) {
            ttsManager.speakEnglish(word)
        }
    }

    fun speakSentence() {
        val sentence = _uiState.value.generatedCard?.exampleSentence ?: return
        ttsManager.speakEnglish(sentence)
    }

    fun speakJapaneseTranslation() {
        val jaText = _uiState.value.generatedCard?.exampleTranslation ?: return
        ttsManager.speakJapanese(jaText)
    }

    fun saveToVocabulary() {
        val card = _uiState.value.generatedCard ?: return
        viewModelScope.launch {
            try {
                wordRepository.saveCard(card)
                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    successMessage = "単語帳に保存しました！"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "保存に失敗しました: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
