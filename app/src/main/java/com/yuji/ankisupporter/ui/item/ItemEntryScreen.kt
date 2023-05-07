package com.yuji.ankisupporter.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuji.ankisupporter.AnkiSupporterTopAppBar
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.WordRecognizer
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
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    WordRecognizer.setItemEntryViewModel(viewModel)

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
                coroutineScope.launch {
                    viewModel.saveItem()
                    navigateBack()
                }
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        ItemInputForm(itemUiState = itemUiState, onValueChange = onItemValueChange)
        Button(
            onClick = onSaveClick,
            enabled = itemUiState.actionEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_action))
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
            singleLine = true
        )
        /*
        OutlinedTextField(
            value = itemUiState.price,
            onValueChange = { onValueChange(itemUiState.copy(price = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            label = { Text(stringResource(R.string.item_price_req)) },
            leadingIcon = { Text(Currency.getInstance(Locale.getDefault()).symbol) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = itemUiState.quantity,
            onValueChange = { onValueChange(itemUiState.copy(quantity = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.quantity_req)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

         */
        Button(onClick = { WordRecognizer.startRecognizer() }) {
            Text(stringResource(R.string.record))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemEntryScreenPreview() {
    AnkiSupporterTheme {
        ItemEntryBody(
            itemUiState = ItemUiState(
                name = "Item name",
                price = "10.00",
                quantity = "5"
            ),
            onItemValueChange = {},
            onSaveClick = {}
        )
    }
}
