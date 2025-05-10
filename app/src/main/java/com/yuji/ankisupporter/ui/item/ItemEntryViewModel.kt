package com.yuji.ankisupporter.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuji.ankisupporter.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * View Model to validate and insert items in the Room database.
 */
class ItemEntryViewModel(
    private val itemsRepository: ItemsRepository,
//    private val chatRepository: ChatRepository
) : ViewModel() {

//    private val _itemEntryViewModelTAG = "Item Entry ViewModel"

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val _savedItemId = MutableStateFlow<Long?>(null)
    val savedItemId: StateFlow<Long?> = _savedItemId.asStateFlow()

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(newItemUiState: ItemUiState) {
        itemUiState = newItemUiState.copy( actionEnabled = newItemUiState.isValid())
    }

    suspend fun saveItem() {
        if (itemUiState.isValid()) {
            viewModelScope.launch {
                val newItemId: Long = itemsRepository.insertItem(itemUiState.toItem())
                _savedItemId.value = newItemId
            }
        }
    }

    fun resetSavedItemId() {
        _savedItemId.value = null
    }
}
