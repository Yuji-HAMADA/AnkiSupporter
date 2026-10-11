package com.example.ankisupporter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ankisupporter.ui.MainApp
import com.example.ankisupporter.ui.home.HomeViewModel
import com.example.ankisupporter.ui.settings.SettingsViewModel
import com.example.ankisupporter.ui.theme.AnkiSupporterTheme
import com.example.ankisupporter.ui.vocabulary.VocabularyViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AnkiSupporterApp

        setContent {
            AnkiSupporterTheme {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return HomeViewModel(
                                aiRepository = app.aiRepository,
                                wordRepository = app.wordRepository,
                                preferences = app.preferences,
                                ttsManager = app.ttsManager
                            ) as T
                        }
                    }
                )

                val vocabularyViewModel: VocabularyViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return VocabularyViewModel(
                                wordRepository = app.wordRepository,
                                ttsManager = app.ttsManager
                            ) as T
                        }
                    }
                )

                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(
                                preferences = app.preferences,
                                aiRepository = app.aiRepository,
                                ttsManager = app.ttsManager
                            ) as T
                        }
                    }
                )

                MainApp(
                    homeViewModel = homeViewModel,
                    vocabularyViewModel = vocabularyViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

