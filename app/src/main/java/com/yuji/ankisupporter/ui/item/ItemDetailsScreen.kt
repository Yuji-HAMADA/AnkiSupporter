package com.yuji.ankisupporter.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuji.ankisupporter.AnkiSupporterTopAppBar
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.ui.AppViewModelProvider
import com.yuji.ankisupporter.ui.navigation.NavigationDestination
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme
import kotlinx.coroutines.launch

object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AnkiSupporterTopAppBar(
                title = stringResource(ItemDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToEditItem(uiState.value.id) },
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_item_title),
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
    ) { innerPadding ->
        ItemDetailsBody(
            itemUiState = uiState.value,
//            onSellItem = { viewModel.reduceQuantityByOne() },
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteItem()
                    navigateBack()
                }
            },
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun ItemDetailsBody(
    itemUiState: ItemUiState,
//    onSellItem: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }
        ItemInputForm(itemUiState = itemUiState, enabled = false)
        /*
        Button(
            onClick = onSellItem,
            modifier = Modifier.fillMaxWidth(),
            enabled = itemUiState.actionEnabled
        ) {
            Text(stringResource(R.string.sell))
        }
         */
        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false }
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier.padding(16.dp),
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsScreenPreview() {
    AnkiSupporterTheme {
        ItemDetailsBody(
            itemUiState = ItemUiState(
                name = "Item name",
                price = "10.00",
                quantity = "5"),
//            onSellItem = {},
            onDelete = {}
        )
    }
}
