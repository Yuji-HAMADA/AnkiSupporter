package com.yuji.ankisupporter.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val itemsRepository: ItemsRepository
//    val chatRepository: ChatRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(AnkiSupporterDatabase.getDatabase(context).itemDao())
    }

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    /*
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

     */

    /*
    private val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }
    */

//    override val chatRepository: ChatRepository by lazy {
//        NetworkChatRepository(chatApiService)
//        get() =
//    }
}