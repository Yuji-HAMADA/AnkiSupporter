package com.example.ankisupporter.ui.vocabulary

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankisupporter.data.db.WordEntity
import com.example.ankisupporter.data.db.WordRepository
import com.example.ankisupporter.speech.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VocabularyTab {
    SAVED,    // マイ単語帳
    HISTORY   // 検索履歴（最大100件）
}

enum class VocabFilter {
    ALL, UNMEMORIZED, MEMORIZED
}

data class VocabularyUiState(
    val selectedTab: VocabularyTab = VocabularyTab.SAVED,
    val words: List<WordEntity> = emptyList(),
    val filter: VocabFilter = VocabFilter.ALL,
    val searchQuery: String = "",
    val isOperating: Boolean = false
)

class VocabularyViewModel(
    private val wordRepository: WordRepository,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _selectedTab = MutableStateFlow(VocabularyTab.SAVED)
    private val _filter = MutableStateFlow(VocabFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isOperating = MutableStateFlow(false)

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val uiState: StateFlow<VocabularyUiState> = combine(
        _selectedTab,
        wordRepository.getSavedWords(),
        wordRepository.getSearchHistory(100),
        _filter,
        _searchQuery,
        _isOperating
    ) { tab, savedWords, historyWords, filter, query, isOperating ->
        val sourceList = if (tab == VocabularyTab.SAVED) savedWords else historyWords

        val filtered = sourceList.filter { word ->
            val matchesFilter = if (tab == VocabularyTab.HISTORY) {
                true
            } else {
                when (filter) {
                    VocabFilter.ALL -> true
                    VocabFilter.UNMEMORIZED -> !word.isMemorized
                    VocabFilter.MEMORIZED -> word.isMemorized
                }
            }
            val matchesQuery = if (query.isBlank()) true else {
                word.word.contains(query, ignoreCase = true) ||
                        word.translation.contains(query, ignoreCase = true) ||
                        word.exampleSentence.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }

        VocabularyUiState(
            selectedTab = tab,
            words = filtered,
            filter = filter,
            searchQuery = query,
            isOperating = isOperating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyUiState()
    )

    fun selectTab(tab: VocabularyTab) {
        _selectedTab.value = tab
    }

    fun setFilter(filter: VocabFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleMemorized(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.toggleMemorized(word.id, !word.isMemorized)
        }
    }

    fun promoteToSaved(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.promoteToSaved(word.id)
            _userMessage.emit("「${word.word}」をマイ単語帳に追加しました")
        }
    }

    fun removeFromSaved(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.removeFromSaved(word.id)
            _userMessage.emit("「${word.word}」をマイ単語帳から解除しました")
        }
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.deleteWord(word)
        }
    }

    fun speakWord(word: String) {
        ttsManager.speakEnglish(word)
    }

    fun speakSentence(sentence: String) {
        ttsManager.speakEnglish(sentence)
    }

    suspend fun exportWordsToJson(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        _isOperating.value = true
        try {
            val words = wordRepository.getAllWordsList()
            if (words.isEmpty()) {
                val error = "エクスポート対象の単語が登録されていません"
                _userMessage.emit(error)
                return@withContext Result.failure(IllegalStateException(error))
            }

            val jsonString = json.encodeToString(words)
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportFile = File(exportDir, "anki_supporter_backup_$timestamp.json")
            exportFile.writeText(jsonString, Charsets.UTF_8)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
            Result.success(contentUri)
        } catch (e: Exception) {
            val error = "エクスポートに失敗しました: ${e.localizedMessage ?: e.message}"
            _userMessage.emit(error)
            Result.failure(e)
        } finally {
            _isOperating.value = false
        }
    }

    fun importWordsFromJson(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isOperating.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("ファイルを開くことができませんでした")

                val jsonContent = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val importedWords = json.decodeFromString<List<WordEntity>>(jsonContent)

                if (importedWords.isEmpty()) {
                    _userMessage.emit("インポート可能な単語データが見つかりませんでした")
                    return@launch
                }

                val count = wordRepository.importWords(importedWords)
                _userMessage.emit("${count}件の単語データをインポートしました")
            } catch (e: Exception) {
                _userMessage.emit("インポートに失敗しました（JSON形式を確認してください）: ${e.localizedMessage ?: e.message}")
            } finally {
                _isOperating.value = false
            }
        }
    }
}
