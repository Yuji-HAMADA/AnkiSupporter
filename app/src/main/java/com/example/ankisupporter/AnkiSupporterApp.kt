package com.example.ankisupporter

import android.app.Application
import com.example.ankisupporter.data.ai.AiRepository
import com.example.ankisupporter.data.ai.AiRepositoryImpl
import com.example.ankisupporter.data.db.AppDatabase
import com.example.ankisupporter.data.db.WordRepository
import com.example.ankisupporter.data.db.WordRepositoryImpl
import com.example.ankisupporter.data.preferences.AppPreferences
import com.example.ankisupporter.speech.TextToSpeechManager

class AnkiSupporterApp : Application() {

    lateinit var preferences: AppPreferences
        private set

    lateinit var aiRepository: AiRepository
        private set

    lateinit var wordRepository: WordRepository
        private set

    lateinit var ttsManager: TextToSpeechManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferences = AppPreferences(this)
        aiRepository = AiRepositoryImpl(preferences)
        val database = AppDatabase.getInstance(this)
        wordRepository = WordRepositoryImpl(database.wordDao())
        ttsManager = TextToSpeechManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
    }

    companion object {
        lateinit var instance: AnkiSupporterApp
            private set
    }
}

