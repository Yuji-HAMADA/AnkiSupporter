package com.yuji.ankisupporter.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.yuji.ankisupporter.WordSpeaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        WordSpeaker.init(getApplication())
//        WordRecognizer.init(getApplication(), this)
    }

    var userInput: String by mutableStateOf("")
        private set

    fun updateInputWord(inputWord: String) {
        userInput = inputWord
//        WordSpeaker.speak(inputWord)
    }
}