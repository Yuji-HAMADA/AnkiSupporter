package com.yuji.ankisupporter.network

import android.util.Log
import com.yuji.ankisupporter.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "https://www.googleapis.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface GoogleCustomSearch {

//    @GET("customsearch/v1?key={apiKey}&cx={engineID}&q=ant+icon&searchType=image")
    @GET("customsearch/v1")
    suspend fun getResponse(
        @Query("key") key: String,
        @Query("cx") cx: String,
        @Query("q") q: String,
        @Query("searchType") searchType: String
    ): String
}

object GoogleCustomSearchApi {
    private const val LOG_TAG = "GoogleCustomSearch"

    private val retrofitService : GoogleCustomSearch by lazy {
        retrofit.create(GoogleCustomSearch::class.java)
    }

    suspend fun getResponse(): String {
        val apiKey = BuildConfig.myGoogleCustomSearchKey
        val engineID = BuildConfig.myGoogleSearchEngineID
        Log.v(LOG_TAG, "Get image started")
        val response = retrofitService.getResponse(
            key = apiKey,
            cx = engineID,
            q = "ant+icon",
            searchType = "image"
        )
        Log.v(LOG_TAG, "Get image finished")
        return response
    }
}