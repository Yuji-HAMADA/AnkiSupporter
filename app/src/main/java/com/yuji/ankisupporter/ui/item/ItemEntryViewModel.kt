package com.yuji.ankisupporter.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.yuji.ankisupporter.data.ItemsRepository

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

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(newItemUiState: ItemUiState) {
        itemUiState = newItemUiState.copy( actionEnabled = newItemUiState.isValid())
    }

    val openAI = OpenAI("YOUR_API_KEY")
    val completionRequest = CompletionRequest(
        model = ModelId("text-ada-001"),
        prompt = "Somebody once told me the world is gonna roll me",
        echo = true
    )

    suspend fun saveItem() {
/*
        try {
            val request = ChatRequest(
                listOf(
                    Message("system", "You are a friendly assistant."),
                    Message("user", "What's the weather like today?")
                )
            )
            val response = chatRepository.getChatResponse(request)
            val messageFromChat: String = response.body()?.obj as? String ?: ""
            Log.v(_itemEntryViewModelTAG, messageFromChat)
        } catch (e: Exception) {

        }
*/

        if (itemUiState.isValid()) {
            itemsRepository.insertItem(itemUiState.toItem())
        }
    }
}
