package com.yuji.ankisupporter

import android.app.Application
import android.util.Log
import com.yuji.ankisupporter.data.AppContainer
import com.yuji.ankisupporter.data.AppDataContainer
import com.yuji.ankisupporter.utility.Translator

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