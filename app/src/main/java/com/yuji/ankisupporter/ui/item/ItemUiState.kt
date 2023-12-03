package com.yuji.ankisupporter.ui.item

import com.yuji.ankisupporter.data.Item

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val id: Int = 0,
    val name: String = "",
    val meaning: String = "",
    val meaningDetail: String = "",
    val level: String = "",
    val eikenLevel: String = "",
    val imageLink: String = "",
//    val price: String = "100",
//    val quantity: String = "1",
    val actionEnabled: Boolean = false
)

/**
 * Extension function to convert [ItemUiState] to [Item]. Similarly if the value of
 * [ItemUiState] is not a valid [Int], then the quantity will be set to 0
 */
fun ItemUiState.toItem(): Item = Item(
    id = id,
    name = name,
    meaning = meaning,
    meaningDetail = meaningDetail,
    level = level,
    eikenLevel = eikenLevel,
    imageLink = imageLink
//    price = price.toDoubleOrNull() ?: 0.0,
//    quantity = quantity.toIntOrNull() ?: 0
)

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(actionEnabled: Boolean = false): ItemUiState = ItemUiState(
    id = id,
    name = name,
    meaning = meaning,
//    price = price.toString(),
//    quantity = quantity.toString(),
    meaningDetail = meaningDetail,
    level = level,
    eikenLevel = eikenLevel,
    imageLink = imageLink,
    actionEnabled = actionEnabled
)

fun ItemUiState.isValid() : Boolean {
//    return name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
//    return name.isNotBlank() && quantity.isNotBlank()
    return name.isNotBlank()
}
