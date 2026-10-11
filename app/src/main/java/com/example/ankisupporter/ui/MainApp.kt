package com.example.ankisupporter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ankisupporter.ui.home.HomeScreen
import com.example.ankisupporter.ui.home.HomeViewModel
import com.example.ankisupporter.ui.settings.SettingsScreen
import com.example.ankisupporter.ui.settings.SettingsViewModel
import com.example.ankisupporter.ui.vocabulary.VocabularyScreen
import com.example.ankisupporter.ui.vocabulary.VocabularyViewModel

enum class AppDestination(val title: String, val icon: ImageVector) {
    HOME("例文生成", Icons.Default.AutoAwesome),
    VOCABULARY("単語帳", Icons.Default.Book),
    SETTINGS("設定", Icons.Default.Settings)
}

@Composable
fun MainApp(
    homeViewModel: HomeViewModel,
    vocabularyViewModel: VocabularyViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val destinations = AppDestination.values()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (destinations[selectedIndex]) {
            AppDestination.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSettings = { selectedIndex = AppDestination.SETTINGS.ordinal },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.VOCABULARY -> {
                VocabularyScreen(
                    viewModel = vocabularyViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

