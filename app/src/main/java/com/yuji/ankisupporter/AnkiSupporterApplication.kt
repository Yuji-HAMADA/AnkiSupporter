package com.yuji.ankisupporter

import android.app.Application
import com.yuji.ankisupporter.data.AppContainer
import com.yuji.ankisupporter.data.AppDataContainer

class AnkiSupporterApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        WordSpeaker.init(this)
        WordRecognizer.init(this)
    }
}