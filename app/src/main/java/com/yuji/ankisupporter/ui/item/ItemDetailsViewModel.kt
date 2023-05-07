package com.yuji.ankisupporter.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuji.ankisupporter.data.ItemsRepository
import kotlinx.coroutines.flow.*

/**
 * ViewModel to retrieve, update and delete an item from the data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    val uiState: StateFlow<ItemUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                it.toItemUiState(actionEnabled = it.quantity > 0 )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemUiState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(uiState.value.toItem())
    }
}