package com.yuji.ankisupporter.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yuji.ankisupporter.AnkiSupporterTopAppBar
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.data.Item
import com.yuji.ankisupporter.ui.AppViewModelProvider
import com.yuji.ankisupporter.ui.navigation.NavigationDestination
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()

    viewModel.setMaxIndex()

    Scaffold(
        topBar = {
            AnkiSupporterTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
//                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.item_entry_title),
                )
            }
        },
    ) { innerPadding ->
        HomeBody(
            homeUiState = homeUiState,
            onItemClick = navigateToItemUpdate,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HomeBody(
    homeUiState: HomeUiState,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (homeUiState.itemList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_item_description),
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            AnkiSupporterList(
                itemList = homeUiState.itemList,
                index = homeUiState.index,
                onItemClick = { onItemClick(it.id) },
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen.padding_small
                    )
                )
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun AnkiSupporterList(
    modifier: Modifier = Modifier,
    itemList: List<Item>,
    index: Int = -1,
    onItemClick: (Item) -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = itemList, key = { it.id }) { item ->
            AnkiSupporterItem(
                item = item,
                onItemClick = onItemClick,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }

    if (index >= 0) {
        coroutineScope.launch {
            listState.scrollToItem(index = index)
        }
    }
}

@Composable
private fun AnkiSupporterItem(
    item: Item,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .weight(2f),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = item.meaning.take(200),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(item.imageLink)
                    .build(),
                contentDescription = null,
                contentScale =  ContentScale.Crop,
                modifier = modifier
                    .size(60.dp)
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AnkiSupporterTheme {
        HomeBody(
            listOf(
                Item(1, "Game", "one", "", "", "", ""),
                Item(2, "Pen", "two", "", "", "", ""),
                Item(3, "TV", "three", "", "", "", "")
            ),
            onItemClick = {}
        )
    }
}
 */
@Preview(showBackground = true)
@Composable
fun AnkiSupporterItemPreview() {
    AnkiSupporterTheme {
        AnkiSupporterItem(
            item = Item(1, "Game", "GH", "", "", "", ""),
            onItemClick = { }
        )
    }
}