package com.yuji.ankisupporter.network

import android.util.Log
import com.yuji.ankisupporter.BuildConfig
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yuji.ankisupporter.model.GoogleCustomSearchData
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://www.googleapis.com"
private val json = Json { ignoreUnknownKeys = true }
private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

interface GoogleCustomSearch {
    @GET("customsearch/v1")
    suspend fun getResponse(
        @Query("key") key: String,
        @Query("cx") cx: String,
        @Query("q") q: String,
        @Query("searchType") searchType: String
    ): GoogleCustomSearchData
}

object GoogleCustomSearchApi {
    private const val LOG_TAG = "GoogleCustomSearch"

    private val retrofitService : GoogleCustomSearch by lazy {
        retrofit.create(GoogleCustomSearch::class.java)
    }

    suspend fun getResponse(word: String): GoogleCustomSearchData {
       Log.v(LOG_TAG, "Get image started")
        val response = retrofitService.getResponse(
            key = BuildConfig.myGoogleCustomSearchKey,
            cx = BuildConfig.myGoogleSearchEngineID,
            q = word + "icon",
            searchType = "image"
        )
        Log.v(LOG_TAG, "Get image finished")
        return response
    }
}