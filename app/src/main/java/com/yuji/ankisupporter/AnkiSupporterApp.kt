package com.yuji.ankisupporter

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.yuji.ankisupporter.R.string
import com.yuji.ankisupporter.ui.navigation.AnkiSupporterNavHost

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun AnkiSupporterApp(navController: NavHostController = rememberNavController()) {
    AnkiSupporterNavHost(navController = navController)
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@Composable
fun AnkiSupporterTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {}
) {
    if (canNavigateBack) {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
        )
    } else {
        TopAppBar(title = { Text(title) }, modifier = modifier)
    }
}