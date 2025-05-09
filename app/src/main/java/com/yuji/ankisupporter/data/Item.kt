package com.yuji.ankisupporter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the database.
 */
@Entity
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val meaning: String,
    val meaningDetail: String,
    val level: String,
    val eikenLevel: String,
    val imageLink: String
//    val price: Double,
//    val : Int
)
