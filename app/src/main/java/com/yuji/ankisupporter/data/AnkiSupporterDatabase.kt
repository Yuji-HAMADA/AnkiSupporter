package com.yuji.ankisupporter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class AnkiSupporterDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    companion object {
        @Volatile
        private var Instance: AnkiSupporterDatabase? = null

        fun getDatabase(context: Context): AnkiSupporterDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AnkiSupporterDatabase::class.java, "item_database")
                    // Setting this option in your app's database builder means that Room
                    // permanently deletes all data from the tables in your database when it
                    // attempts to perform a migration with no defined migration path.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}