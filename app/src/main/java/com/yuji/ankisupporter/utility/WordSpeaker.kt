package com.yuji.ankisupporter.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object WordSpeaker : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    fun init(context: Context) {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale.US
            if (tts!!.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts!!.language = Locale.US
            }

            tts!!.speak(
                "Hello guys",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "utteranceId"
            )
        }
    }

    fun speak(string: String) {
        tts!!.speak(
            string,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "utteranceId"
        )
    }
}