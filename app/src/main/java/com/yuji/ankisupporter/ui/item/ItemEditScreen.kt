package com.yuji.ankisupporter.ui.item

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuji.ankisupporter.AnkiSupporterTopAppBar
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.utility.WordRecognizer
import com.yuji.ankisupporter.ui.AppViewModelProvider
import com.yuji.ankisupporter.ui.navigation.NavigationDestination
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme
import com.yuji.ankisupporter.utility.Translator
import kotlinx.coroutines.launch

object ItemEditDestination : NavigationDestination {
    override val route = "item_edit"
    override val titleRes = R.string.edit_item_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@Composable
fun ItemEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val itemEditScreenTAG = "ItemEditScreen"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    WordRecognizer.setItemEditViewModel(viewModel)

    Log.v(itemEditScreenTAG, "ItemEditScreen Start")

    Scaffold(
        topBar = {
            AnkiSupporterTopAppBar(
                title = stringResource(ItemEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                Log.v(itemEditScreenTAG, "Save Start")
                Translator.englishJapaneseTranslator.translate(viewModel.itemUiState.name)
                    .addOnSuccessListener { translatedText ->
                        viewModel.updateUiState(
                            viewModel.itemUiState.copy(meaning = translatedText)
                        )
                        Log.v(itemEditScreenTAG, viewModel.itemUiState.name)
                        Log.v(itemEditScreenTAG, viewModel.itemUiState.meaning)
                        coroutineScope.launch {
                            viewModel.updateItem()
                            navigateBack()
                        }
                    }
            },
            onOpenWebsite = {
                val url = "https://www.playphrase.me/#/search?q=" + viewModel.itemUiState.name
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ItemEditRoutePreview() {
    AnkiSupporterTheme {
        ItemEditScreen(
            navigateBack = { /*Do nothing*/ },
            onNavigateUp = { /*Do nothing*/ }
        )
    }
}
