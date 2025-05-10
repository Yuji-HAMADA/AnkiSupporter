package com.yuji.ankisupporter.ui.item

import com.yuji.ankisupporter.network.WeblioApi
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuji.ankisupporter.AnkiSupporterTopAppBar
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.network.GoogleCustomSearchApi
import com.yuji.ankisupporter.utility.WordRecognizer
import com.yuji.ankisupporter.ui.AppViewModelProvider
import com.yuji.ankisupporter.ui.navigation.NavigationDestination
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme
import kotlinx.coroutines.launch

object ItemEntryDestination : NavigationDestination {
    override val route = "item_entry"
    override val titleRes = R.string.item_entry_title
}

@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    navigateToItemDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val itemEntryScreenTAG = "ItemEntryScreen"
    val savedItemId by viewModel.savedItemId.collectAsState()

    val context = LocalContext.current

    WordRecognizer.setItemEntryViewModel(viewModel)

    LaunchedEffect(savedItemId) {
        savedItemId?.let { itemId ->
            navigateToItemDetails(itemId.toInt()) // Long -> Int に変換
            viewModel.resetSavedItemId()
        }
    }

    Scaffold(
        topBar = {
            AnkiSupporterTopAppBar(
                title = stringResource(ItemEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                Log.v(itemEntryScreenTAG, "Save Start")

                coroutineScope.launch {
                    Log.v(itemEntryScreenTAG, "saving item...")
                    val meaning = WeblioApi.getMeaning(viewModel.itemUiState.name)
//                        .substring(0, 100)
                    val googleCustomSearchData = GoogleCustomSearchApi.getResponse(
                        viewModel.itemUiState.name
                    )
                    val imageLink = googleCustomSearchData.items[0].link
                    viewModel.updateUiState(
                        viewModel.itemUiState.copy(
                            meaning = meaning,
                            imageLink = imageLink
                        )
                    )
                    viewModel.saveItem()
                    Log.v(itemEntryScreenTAG, "Item saved")
//                    navigateBack()
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

@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemUiState) -> Unit,
    onSaveClick: () -> Unit,
    onOpenWebsite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        ItemInputForm(itemUiState = itemUiState, onValueChange = onItemValueChange)
        Button(onClick = { WordRecognizer.startRecognizer() }) {
            Text(stringResource(R.string.record))
        }
        Button(
            onClick = onSaveClick,
            enabled = itemUiState.actionEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_action))
        }
        Button(
            onClick = onOpenWebsite,
            enabled = itemUiState.actionEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.play_with_this_word))
        }
    }
}

@Composable
fun ItemInputForm(
    itemUiState: ItemUiState,
    modifier: Modifier = Modifier,
    onValueChange: (ItemUiState) -> Unit = {},
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = itemUiState.name,
            onValueChange = { onValueChange(itemUiState.copy(name = it)) },
            label = { Text(stringResource(R.string.item_name_req)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemEntryScreenPreview() {
    AnkiSupporterTheme {
        ItemEntryBody(
            itemUiState = ItemUiState(
                name = "Item name",
//                price = "10.00",
//                quantity = "5"
            ),
            onItemValueChange = {},
            onSaveClick = {},
            onOpenWebsite = {}
        )
    }
}
