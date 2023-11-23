package com.yuji.ankisupporter.network

import android.util.Log
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL =
    "https://ejje.weblio.jp"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface WeblioApiService {
    @GET("content/{word}")
    suspend fun getResponse(@Path("word") word: String): String
}

object WeblioApi {
    private const val LOG_TAG = "WeblioApi"

    private val retrofitService : WeblioApiService by lazy {
        retrofit.create(WeblioApiService::class.java)
    }

    suspend fun getMeaning(word: String): String {
        Log.v(LOG_TAG, "Get meaning started")
        val response = retrofitService.getResponse(word = word)
        Log.v(LOG_TAG, "Get meaning finished")
        return Jsoup.parse(response).getElementsByClass("content-explanation  ej").text()
    }
}
