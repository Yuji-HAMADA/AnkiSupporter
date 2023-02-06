package com.yuji.ankisupporter.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class AnkiSupporterScreen() {
    Start,
    List
}

@Composable
fun AnkiSupporterScreen (
//    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AnkiSupporterScreen.Start.name
//        modifier = modifier.padding()
    ) {
        composable(route = AnkiSupporterScreen.Start.name) {
            AppScreen(
                score = uiState.score,
                inputWord = viewModel.userInput,
                onWordChanged = { viewModel.updateInputWord(it) },
                onNextButtonClicked = { navController.navigate(AnkiSupporterScreen.List.name) }
            )
        }

        composable(route = AnkiSupporterScreen.List.name) {
            WordListScreen()
        }
    }
}