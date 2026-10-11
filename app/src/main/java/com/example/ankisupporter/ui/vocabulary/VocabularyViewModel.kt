package com.example.ankisupporter.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankisupporter.data.db.WordEntity
import com.example.ankisupporter.data.db.WordRepository
import com.example.ankisupporter.speech.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VocabFilter {
    ALL, UNMEMORIZED, MEMORIZED
}

data class VocabularyUiState(
    val words: List<WordEntity> = emptyList(),
    val filter: VocabFilter = VocabFilter.ALL,
    val searchQuery: String = ""
)

class VocabularyViewModel(
    private val wordRepository: WordRepository,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _filter = MutableStateFlow(VocabFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<VocabularyUiState> = combine(
        wordRepository.getAllWords(),
        _filter,
        _searchQuery
    ) { allWords, filter, query ->
        val filtered = allWords.filter { word ->
            val matchesFilter = when (filter) {
                VocabFilter.ALL -> true
                VocabFilter.UNMEMORIZED -> !word.isMemorized
                VocabFilter.MEMORIZED -> word.isMemorized
            }
            val matchesQuery = if (query.isBlank()) true else {
                word.word.contains(query, ignoreCase = true) ||
                        word.translation.contains(query, ignoreCase = true) ||
                        word.exampleSentence.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
        VocabularyUiState(words = filtered, filter = filter, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyUiState()
    )

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
}

