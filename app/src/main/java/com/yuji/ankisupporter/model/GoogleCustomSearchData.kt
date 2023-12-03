package com.yuji.ankisupporter.model

import kotlinx.serialization.Serializable

@Serializable
data class GoogleCustomSearchData(
    val kind: String,
    val items: List<GoogleCustomSearchItem>
)

@Serializable
data class GoogleCustomSearchItem(
    val link: String,
)
