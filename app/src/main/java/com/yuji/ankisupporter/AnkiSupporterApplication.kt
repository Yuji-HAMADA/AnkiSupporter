package com.yuji.ankisupporter

import android.app.Application
import android.util.Log
import com.yuji.ankisupporter.data.AppContainer
import com.yuji.ankisupporter.data.AppDataContainer
import com.yuji.ankisupporter.utility.Translator
import com.yuji.ankisupporter.utility.WordRecognizer
import com.yuji.ankisupporter.utility.WordSpeaker
import dagger.hilt.android.HiltAndroidApp

/**
 * AnkiSupporterApplication class is the main entry point of the application.
 * It initializes the app container and other necessary components.
 */
@HiltAndroidApp
class AnkiSupporterApplication : Application() {
    private val ankiSupporterAppTAG = "AnkiSupporterApp"

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        Log.v(ankiSupporterAppTAG, "Application onCreate called")


        WordSpeaker.init(this)
        WordRecognizer.init(this)
        Translator.init()
    }
}